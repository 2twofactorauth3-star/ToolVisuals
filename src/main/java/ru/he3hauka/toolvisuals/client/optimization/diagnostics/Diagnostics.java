package ru.he3hauka.toolvisuals.client.optimization.diagnostics;

import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

import java.nio.file.Path;
import java.nio.file.NoSuchFileException;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.capture.*;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.profile.*;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.report.*;

public final class Diagnostics {
    public enum Section {HUD,WORLD_EXTRACT,WORLD_SUBMIT,TEXTURE_UPLOAD,ENTITY_SCAN,TAB,EFFECT_TICK,CLIENT_TICK,RENDER,
        VANILLA_EXTRACT,ENTITY_EXTRACT,BLOCK_ENTITY_EXTRACT,ENTITY_SUBMIT,BLOCK_ENTITY_SUBMIT,PARTICLE_TICK,PARTICLE_EXTRACT,
        CHUNK_SCHEDULE,CHUNK_UPLOAD,CHUNK_BUILD,OCCLUSION}
    public enum Counter {ENTITY_CANDIDATES,BLOCK_ENTITY_CANDIDATES,OCCLUDED,QUADS_SKIPPED,CHEST_CACHE_HIT,CHEST_CACHE_MISS,CHUNK_BACKGROUND,CHUNK_URGENT,CHUNK_COMPLETED,OCCLUSION_TESTS,OCCLUSION_CELLS,EQUIPMENT_CACHE_HIT,EQUIPMENT_CACHE_MISS,ITEM_BATCHED}
    private static final FrameHistory HISTORY=new FrameHistory(2048);
    private static volatile boolean enabled,active;
    private static volatile FrameHistory.Snapshot view=HISTORY.snapshot();
    private static final LongAdder[] COUNTERS=java.util.Arrays.stream(Counter.values()).map(value->new LongAdder()).toArray(LongAdder[]::new);
    private static volatile DiagnosticContext context=DiagnosticContext.INACTIVE;
    private static volatile FreezeMonitor monitor=new FreezeMonitor();
    private static final ArrayBlockingQueue<FreezeEvent> EVENTS=new ArrayBlockingQueue<>(16);
    private static final ProfileEvidence EVIDENCE=new ProfileEvidence();
    private static volatile long owner;
    private static ScheduledExecutorService worker;
    private static FlightProfile profile;
    private static DetailBudget budget=new DetailBudget();
    private static Supplier<String> metadata=()->"";
    private static boolean header,saturated,profileAttempted;
    private static long nextMarker,knownSize,epochNanos;
    private static final LongAdder DROPPED=new LongAdder();
    private static Instant epochWall;
    private static RuntimeProbe.Gc lastGc;
    private static volatile long viewTime;
    private Diagnostics(){}
    public static boolean enabled(){return enabled;}
    public static boolean active(){return active;}
    public static FrameHistory.Snapshot view(){long now=System.nanoTime();if(active&&now-viewTime>100_000_000L){view=HISTORY.snapshot();viewTime=now;}return view;}
    public static long start(Section section){if(!active)return 0;long now=System.nanoTime();if(Thread.currentThread().threadId()==owner)monitor.enter(section,now);return now;}
    public static void end(Section section,long started){if(started!=0&&active&&Thread.currentThread().threadId()==owner)monitor.exit(section,System.nanoTime(),started);}
    public static void count(Counter counter,long amount){if(active)COUNTERS[counter.ordinal()].add(amount);}
    public static long beginFrame(DiagnosticContext next){
        if(!enabled)return 0;
        long now=System.nanoTime();
        if(!active||context.worldRevision()!=next.worldRevision()){HISTORY.clear();for(var counter:COUNTERS)counter.reset();}
        context=next;
        if(!next.eligible()){suspend();return 0;}
        active=true;owner=Thread.currentThread().threadId();
        return monitor.begin(now,owner,next,RuntimeProbe.current());
    }
    public static void endFrame(long token,DiagnosticContext next){
        if(token==0||!enabled)return;
        long now=System.nanoTime();var event=monitor.end(token,now,next,RuntimeProbe.current());
        active=monitor.playing();context=next;
        if(event!=null)offer(event);
        if(active&&frameStarted!=0)HISTORY.add(now-frameStarted);
        frameStarted=0;
    }
    private static long frameStarted;
    public static long openFrame(DiagnosticContext next){long token=beginFrame(next);frameStarted=token==0?0:monitor.started();return token;}
    public static void suspend(){active=false;monitor.suspend(System.nanoTime());HISTORY.clear();frameStarted=0;}
    private static void offer(FreezeEvent event){if(!EVENTS.offer(event))DROPPED.increment();}
    public static synchronized void initialize(Path marker){
        initialize(marker,()->"");
    }
    public static synchronized void initialize(Path marker,Supplier<String> headerMetadata){
        if(worker!=null)return;
        var log=new MarkerLog(marker);
        metadata=headerMetadata;epochWall=Instant.now();epochNanos=System.nanoTime();
        worker=Executors.newSingleThreadScheduledExecutor(r->{var thread=new Thread(r,"ToolVisuals diagnostics");thread.setDaemon(true);return thread;});
        schedule(log,0);
    }
    private static synchronized void schedule(MarkerLog log,long delay){if(worker!=null&&!worker.isShutdown())worker.schedule(ClientErrors.task("Performance diagnostics worker",()->poll(log)),delay,TimeUnit.MILLISECONDS);}
    private static synchronized void poll(MarkerLog log){
        try{
            long now=System.nanoTime();
            if(now>=nextMarker){
                nextMarker=now+1_000_000_000L;
                long size=log.size();
                if(size<0){disable();return;}
                if(saturated&&size<knownSize){saturated=false;header=false;}
                if(saturated)return;
                if(!enabled){enabled=true;header=false;monitor=new FreezeMonitor();budget=new DetailBudget();EVIDENCE.clear();lastGc=RuntimeProbe.gc();}
                knownSize=size;
            }
            if(!enabled)return;
            if(!active)lastGc=RuntimeProbe.gc();
            if(active&&!header){
                if(!write(log,"\nToolVisuals diagnostics v2 | "+Instant.now()+"\nТолько активная игра. Отдельные фризы имеют номер; нормальные интервалы не перекрываются. Отчёт раз в 30 s активной игры.\nGC accounting не равно паузе; JFR allocation weight является сэмплированной оценкой. CPU-этапы не измеряют исполнение на GPU.\nНе записываются ники, UUID, чат, адрес сервера, аргументы запуска с токенами или пути пользователя. Лимит файла 2 MiB.\n"+metadata.get()+"\n"))return;
                header=true;
            }
            if(active&&!profileAttempted){
                profileAttempted=true;
                try{profile=new FlightProfile(EVIDENCE,time->monitor.wasActive(time),()->owner,ClientErrors::report);profile.start();}
                catch(RuntimeException|LinkageError error){ClientErrors.report("Start performance JFR sampling",error);}
            }
            if(active){
                var watch=monitor.watch(now);
                if(watch!=null)monitor.sampled(watch,StackSampler.capture(watch,now));
                var pending=monitor.ongoing(now,RuntimeProbe.threadPoint(owner));if(pending!=null)offer(pending);
            }
            while(!EVENTS.isEmpty()){
                var event=EVENTS.peek();
                if(event.finished()&&now-event.ended()<2_000_000_000L)break;
                EVENTS.poll();
                if(budget.allow(event,now)&&!write(log,FreezeReport.event(event,EVIDENCE,profile==null?"unavailable":profile.status(),epochWall.plusNanos(event.ended()-epochNanos))))return;
            }
            if(active){
                var summary=monitor.summary(now,false);
                if(summary!=null){
                    var gc=RuntimeProbe.gc();String text=FreezeReport.summary(summary,Instant.now(),RuntimeProbe.threadPoint(owner),lastGc,gc)+counters()+budget.suppressed();lastGc=gc;
                    long dropped=DROPPED.sumThenReset();if(dropped>0)text+="Очередь диагностики ограничена: пропущено "+dropped+" событий.\n";
                    if(!write(log,text))return;
                }
            }
        }catch(NoSuchFileException error){disable();}
        catch(Exception error){disable();ClientErrors.report("Write performance diagnostic marker",error);}
        finally{schedule(log,active?25:enabled?200:1000);}
    }
    private static boolean write(MarkerLog log,String text) throws java.io.IOException {
        if(log.append(text)){knownSize+=text.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;return true;}
        saturated=true;stopCapture();return false;
    }
    private static String counters() {
        var text=new StringBuilder();
        for(var counter:Counter.values()){long value=COUNTERS[counter.ordinal()].sumThenReset();if(value!=0)text.append(' ').append(counter).append('=').append(value);}
        return text.isEmpty()?"":"Счётчики оптимизаций за этот интервал:"+text+'\n';
    }
    private static void stopProfile(){var current=profile;profile=null;if(current!=null)current.close();profileAttempted=false;}
    private static void stopCapture(){enabled=false;suspend();EVENTS.clear();stopProfile();EVIDENCE.clear();DROPPED.reset();}
    private static void disable(){stopCapture();header=false;saturated=false;knownSize=0;}
    public static synchronized void close(){enabled=false;active=false;if(worker!=null){worker.shutdownNow();worker=null;}disable();nextMarker=0;DROPPED.reset();view=HISTORY.snapshot();}
}
