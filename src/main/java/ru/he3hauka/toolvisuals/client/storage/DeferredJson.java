package ru.he3hauka.toolvisuals.client.storage;

import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class DeferredJson {
    private static final long QUIET_NANOS=200_000_000L, MAX_DELAY_NANOS=2_000_000_000L;
    private static final ScheduledThreadPoolExecutor WORKER=worker();
    private record Pending(Supplier<String> snapshot){}
    private final JsonFile file;
    private final Object writeLock=new Object();
    private Pending pending;
    private ScheduledFuture<?> task;
    private long firstChange,lastChange;
    private final Runnable drainTask=ClientErrors.task("Save deferred configuration",this::drain);
    public DeferredJson(JsonFile file){this.file=file;}
    private static ScheduledThreadPoolExecutor worker(){
        var executor=new ScheduledThreadPoolExecutor(1,job->{var thread=new Thread(job,"ToolVisuals settings");thread.setDaemon(true);return thread;});
        executor.setRemoveOnCancelPolicy(true);return executor;
    }
    public synchronized void request(Supplier<String> snapshot){
        queue(snapshot);
        if(task==null&&file.status()!=JsonFile.Status.LOAD_FAILED)task=WORKER.schedule(drainTask,QUIET_NANOS,TimeUnit.NANOSECONDS);
    }
    private void queue(Supplier<String> snapshot){long now=System.nanoTime();if(pending==null)firstChange=now;lastChange=now;pending=new Pending(snapshot);file.dirty();}
    private void drain(){
        Pending selected;
        synchronized(this){
            task=null;if(pending==null)return;
            long remaining=Math.min(lastChange+QUIET_NANOS,firstChange+MAX_DELAY_NANOS)-System.nanoTime();
            if(remaining>0){task=WORKER.schedule(drainTask,remaining,TimeUnit.NANOSECONDS);return;}
            selected=pending;
        }
        try{write(selected);}catch(IOException error){org.slf4j.LoggerFactory.getLogger("ToolVisuals").warn("Visual changes are not saved; original configuration preserved",error);}
    }
    private void write(Pending selected) throws IOException {
        synchronized(writeLock){
            synchronized(this){if(pending!=selected)return;}
            file.writeSnapshot(selected.snapshot);
            synchronized(this){if(pending==selected)pending=null;else file.dirty();}
        }
    }
    public void flush() throws IOException {
        while(true){
            Pending selected;
            synchronized(this){if(task!=null)task.cancel(false);task=null;selected=pending;}
            if(selected==null)return;
            write(selected);
        }
    }
    public void write(Supplier<String> snapshot) throws IOException {synchronized(this){cancel();queue(snapshot);}flush();}
    public synchronized void cancel(){if(task!=null)task.cancel(false);task=null;pending=null;}
    public void cancelAndAwait(){cancel();synchronized(writeLock){}}
}
