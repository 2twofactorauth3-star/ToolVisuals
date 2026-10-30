package ru.he3hauka.toolvisuals.client.optimization.diagnostics.profile;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.LongPredicate;
import java.util.function.LongSupplier;
import jdk.jfr.FlightRecorder;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedThread;
import jdk.jfr.consumer.RecordingStream;

public final class FlightProfile implements AutoCloseable {
    private final ProfileEvidence evidence;
    private final LongPredicate active;
    private final LongSupplier owner;
    private final BiConsumer<String,Throwable> errors;
    private final Instant epoch=Instant.now();
    private final long origin=System.nanoTime();
    private RecordingStream stream;
    private volatile String status="not_started";
    public FlightProfile(ProfileEvidence evidence,LongPredicate active,LongSupplier owner,BiConsumer<String,Throwable> errors) {
        this.evidence=evidence;this.active=active;this.owner=owner;this.errors=errors;
    }
    public boolean start() {
        if(stream!=null)return true;
        if(!FlightRecorder.isAvailable()){status="unsupported";return false;}
        RecordingStream recording=null;
        try {
            recording=new RecordingStream();
            recording.setMaxAge(Duration.ofSeconds(20));recording.setMaxSize(8*1024*1024);recording.setReuse(true);
            recording.enable("jdk.ObjectAllocationSample").with("throttle","100/s").withStackTrace();
            recording.enable("jdk.ExecutionSample").withPeriod(Duration.ofMillis(20));
            recording.enable("jdk.GCPhasePause");
            for(String event:new String[]{"jdk.ThreadSleep","jdk.ThreadPark","jdk.JavaMonitorEnter","jdk.FileRead","jdk.FileWrite","jdk.SocketRead"})
                recording.enable(event).withThreshold(Duration.ofMillis(50)).withStackTrace();
            recording.onEvent(this::event);
            recording.onError(error->{status="error";errors.accept("Performance JFR event stream",error);});
            recording.onClose(()->{if(status.equals("running"))status="stopped";});
            recording.startAsync();stream=recording;status="running";return true;
        } catch(RuntimeException|Error error) {
            status="unavailable";
            if(recording!=null)try{recording.close();}catch(RuntimeException|Error closing){error.addSuppressed(closing);}
            throw error;
        }
    }
    private long nano(Instant instant) { return origin+Duration.between(epoch,instant).toNanos(); }
    private void event(RecordedEvent event) {
        long from=nano(event.getStartTime()),to=nano(event.getEndTime());
        if(!active.test(to))return;
        String name=event.getEventType().getName();
        var kind=name.equals("jdk.ObjectAllocationSample")?ProfileEvidence.Kind.ALLOCATION:name.equals("jdk.ExecutionSample")?ProfileEvidence.Kind.CPU
                :name.equals("jdk.GCPhasePause")?ProfileEvidence.Kind.GC_PAUSE:ProfileEvidence.Kind.BLOCK;
        RecordedThread thread=kind==ProfileEvidence.Kind.CPU?event.getThread("sampledThread"):event.getThread();
        long id=thread==null?0:thread.getJavaThreadId();
        if(kind!=ProfileEvidence.Kind.ALLOCATION&&kind!=ProfileEvidence.Kind.GC_PAUSE&&id!=owner.getAsLong())return;
        var trace=event.getStackTrace();var frames=new ArrayList<String>();
        if(trace!=null)for(var frame:trace.getFrames()) {
            if(frames.size()==12)break;
            var method=frame.getMethod();String type=method.getType().getName();
            frames.add(type+'.'+method.getName()+':'+frame.getLineNumber());
        }
        if(!frames.isEmpty()&&(frames.getFirst().contains("client.optimization.diagnostics.")||frames.getFirst().startsWith("jdk.jfr.")))return;
        String subject=kind==ProfileEvidence.Kind.ALLOCATION?event.getClass("objectClass").getName():name;
        long weight=kind==ProfileEvidence.Kind.ALLOCATION?event.getLong("weight"):kind==ProfileEvidence.Kind.CPU?1:Math.max(0,to-from);
        evidence.add(new ProfileEvidence.Sample(from,to,kind,id,subject,weight,frames));
    }
    public String status() { return status; }
    @Override public void close() { var recording=stream;stream=null;if(recording!=null)recording.close();status="stopped"; }
}
