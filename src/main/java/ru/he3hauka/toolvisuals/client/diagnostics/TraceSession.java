package ru.he3hauka.toolvisuals.client.diagnostics;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongSupplier;
import ru.he3hauka.toolvisuals.client.module.VisualModule;

final class TraceSession {
    static final long MAX_BYTES=8L*1024*1024;
    static final int QUEUE_LIMIT=512,EVENTS_PER_SECOND=200;
    enum Stop { MANUAL,DISCONNECTED,CONNECTION_CHANGED,WORLD_UNLOADED,CLIENT_CLOSED,SIZE_LIMIT,IO_ERROR }
    record Result(Path path,Stop reason,Throwable failure){}
    private record Entry(long time,String subject,String event,String detail){}
    final Path path;
    final Object connection;
    final CompletableFuture<Path> opened=new CompletableFuture<>();
    final CompletableFuture<Result> finished=new CompletableFuture<>();
    private final List<String> header;
    private final TraceText text;
    private final LongSupplier clock;
    private final long created,maximum;
    private final ArrayBlockingQueue<Entry> queue=new ArrayBlockingQueue<>(QUEUE_LIMIT);
    private final LinkedHashMap<String,String> states=new LinkedHashMap<>();
    private final LongAdder[] disabled=counters(),enabled=counters(),hud=counters();
    private final LongAdder dropped=new LongAdder(),droppedTotal=new LongAdder();
    private volatile boolean accepting=true;
    private volatile Stop reason=Stop.MANUAL;
    private long window;
    private int events;

    TraceSession(Path path,Object connection,List<String> header,TraceText text){this(path,connection,header,text,System::nanoTime,MAX_BYTES);}
    TraceSession(Path path,Object connection,List<String> header,TraceText text,LongSupplier clock,long maximum){
        if(maximum<1024||header.size()>2048)throw new IllegalArgumentException("Invalid trace bounds");
        this.path=path;this.connection=connection;this.header=List.copyOf(header);this.text=text;this.clock=clock;this.maximum=maximum;
        created=window=clock.getAsLong();
    }

    private static LongAdder[] counters(){return Arrays.stream(VisualModule.values()).map(module->new LongAdder()).toArray(LongAdder[]::new);}
    boolean active(){return accepting;}
    void start(Executor executor){
        try{executor.execute(this::write);}catch(RuntimeException error){fail(error);}
    }
    void check(VisualModule module,boolean result){
        if(!accepting)return;
        (result?enabled:disabled)[module.ordinal()].increment();
    }
    void hud(VisualModule module){if(accepting)hud[module.ordinal()].increment();}
    synchronized void event(String subject,String event,String detail){enqueue(subject,event,detail);}
    synchronized void state(String subject,String channel,String detail){
        if(!accepting)return;
        String key=TraceText.clip(subject,80)+"/"+TraceText.clip(channel,80),value=TraceText.clip(detail,TraceText.LIMIT);
        if(value.equals(states.get(key)))return;
        if(enqueue(subject,channel,value)){
            if(!states.containsKey(key)&&states.size()>=256)states.remove(states.keySet().iterator().next());
            states.put(key,value);
        }
    }
    private boolean enqueue(String subject,String event,String detail){
        if(!accepting)return false;
        long now=clock.getAsLong();
        if(now-window>=1_000_000_000L){window=now;events=0;}
        if(events>=EVENTS_PER_SECOND||!queue.offer(new Entry(now,TraceText.clip(subject,80),TraceText.clip(event,80),TraceText.clip(detail,TraceText.LIMIT)))){drop(1);return false;}
        events++;return true;
    }
    private void drop(long count){dropped.add(count);droppedTotal.add(count);}
    void omitted(){drop(1);}
    boolean scope(Object current,boolean worldAvailable){
        if(current!=connection)stop(current==null?Stop.DISCONNECTED:Stop.CONNECTION_CHANGED);
        else if(!worldAvailable)stop(Stop.WORLD_UNLOADED);
        return accepting;
    }
    synchronized boolean stop(Stop reason){
        if(!accepting)return false;
        this.reason=reason;accepting=false;return true;
    }
    private synchronized void limit(){accepting=false;reason=Stop.SIZE_LIMIT;drop(queue.size());queue.clear();}

    private void write(){
        Throwable failure=null;
        try{
            Files.createDirectories(path.getParent());
            try(var stream=new BufferedOutputStream(Files.newOutputStream(path,StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE,LinkOption.NOFOLLOW_LINKS))){
                var output=new Output(stream);
                output.line("ToolVisuals module log "+Instant.now(),true);
                for(String line:header)if(!output.line("HEADER "+text.safe(line),false))break;
                stream.flush();opened.complete(path);
                long flushed=clock.getAsLong();
                while(accepting||!queue.isEmpty()){
                    var entry=queue.poll(200,TimeUnit.MILLISECONDS);
                    if(entry!=null&&!output.line(format(entry),false))break;
                    long now=clock.getAsLong();
                    if(now-flushed>=1_000_000_000L){
                        drain(output);stream.flush();flushed=now;
                    }
                }
                drain(output);
                output.line("STOP reason="+reason+" elapsed_ms="+Math.max(0,(clock.getAsLong()-created)/1_000_000)+" dropped="+droppedTotal.sum(),true);
                stream.flush();
            }
        }catch(IOException|RuntimeException error){failure=error;stop(Stop.IO_ERROR);reason=Stop.IO_ERROR;opened.completeExceptionally(error);}
        catch(InterruptedException error){Thread.currentThread().interrupt();failure=error;stop(Stop.IO_ERROR);reason=Stop.IO_ERROR;opened.completeExceptionally(error);}
        finally{synchronized(this){accepting=false;states.clear();queue.clear();}finished.complete(new Result(path,reason,failure));}
    }

    private void fail(RuntimeException error){accepting=false;reason=Stop.IO_ERROR;opened.completeExceptionally(error);finished.complete(new Result(path,reason,error));}
    private String format(Entry entry){return "t="+Math.max(0,(entry.time-created)/1_000_000)+"ms "+text.safe(entry.subject)+" "+text.safe(entry.event)+" "+text.safe(entry.detail);}
    private void drain(Output output) throws IOException {
        for(var module:VisualModule.values()){
            int index=module.ordinal();long off=disabled[index].sumThenReset(),on=enabled[index].sumThenReset();
            if(off+on>0&&!output.line("CHECK module="+module.title+" enabled="+on+" disabled="+off+" (checks are not proof of a successful action)",false))break;
            long placements=hud[index].sumThenReset();
            if(placements>0&&!output.line("HUD_PLACEMENT module="+module.title+" count="+placements,false))break;
        }
        long lost=dropped.sumThenReset();if(lost>0)output.line("DROPPED events="+lost+" (rate or queue limit)",false);
    }

    private final class Output {
        private final OutputStream stream;
        private long bytes;
        Output(OutputStream stream){this.stream=stream;}
        boolean line(String value,boolean footer) throws IOException {
            byte[] data=(value+"\n").getBytes(StandardCharsets.UTF_8);
            if(bytes+data.length>maximum-(footer?0:256)){limit();return false;}
            stream.write(data);bytes+=data.length;return true;
        }
    }
}
