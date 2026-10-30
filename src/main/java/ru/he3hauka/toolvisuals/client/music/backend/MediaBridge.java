package ru.he3hauka.toolvisuals.client.music.backend;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import ru.he3hauka.toolvisuals.client.music.model.MediaAction;

public final class MediaBridge implements AutoCloseable {
    private static final java.util.concurrent.atomic.AtomicLong EPOCHS=new java.util.concurrent.atomic.AtomicLong();
    @FunctionalInterface public interface Launcher { Process launch() throws IOException; }
    public enum Status { STARTING, READY, UNAVAILABLE, CLOSED }
    public record Timing(long heartbeatMillis,long timeoutMillis,long reconnectMillis) {
        public Timing { if(heartbeatMillis<1||timeoutMillis<=heartbeatMillis||reconnectMillis<1)throw new IllegalArgumentException("Invalid media bridge timing"); }
    }
    public interface Listener {
        void status(long generation,Status status);
        void event(long generation,MediaProtocol.Event event);
    }
    private record Command(long generation,String line) {}
    private final Launcher launcher;
    private final Listener listener;
    private final BiConsumer<String,Throwable> errors;
    private final Timing timing;
    private final ArrayBlockingQueue<Command> commands=new ArrayBlockingQueue<>(16);
    private final Thread worker;
    private volatile boolean closed,ready;
    private volatile long generation,lastResponse;
    private volatile Process process;

    public MediaBridge(Launcher launcher,Listener listener,BiConsumer<String,Throwable> errors) {
        this(launcher,listener,errors,new Timing(3000,8000,5000));
    }
    public MediaBridge(Launcher launcher,Listener listener,BiConsumer<String,Throwable> errors,Timing timing) {
        this.launcher=launcher; this.listener=listener; this.errors=errors; this.timing=timing;
        worker=Thread.ofVirtual().name("ToolVisuals media bridge").unstarted(this::supervise);
    }
    public void start() { worker.start(); }
    public boolean send(long expectedGeneration,long request,MediaAction action,String session,String track,String target,double value) {
        return !closed && ready && generation==expectedGeneration && commands.offer(new Command(expectedGeneration,MediaProtocol.command(request,action,session,track,target,value)));
    }
    private void supervise() {
        int failures=0;
        while (!closed) {
            long epoch=EPOCHS.incrementAndGet(); generation=epoch; ready=false; commands.clear(); listener.status(epoch,Status.STARTING);
            long started=System.nanoTime();
            try { connect(epoch); }
            catch (InterruptedException error) { if (!closed) errors.accept("Wait for media helper",error); Thread.currentThread().interrupt(); break; }
            catch (Exception error) { if (!closed) errors.accept("Connect to Windows media sessions",error); }
            finally { ready=false; commands.clear(); terminate(); }
            if (closed) break;
            listener.status(epoch,Status.UNAVAILABLE);
            failures=System.nanoTime()-started>Duration.ofMinutes(1).toNanos()?1:Math.min(failures+1,6);
            try { Thread.sleep(Duration.ofMillis(Math.min(30_000,timing.reconnectMillis*failures))); }
            catch (InterruptedException error) { if (!closed) errors.accept("Reconnect Windows media helper",error); Thread.currentThread().interrupt(); break; }
        }
        listener.status(generation,Status.CLOSED);
    }
    private void connect(long epoch) throws IOException,InterruptedException {
        Process child=launcher.launch(); process=child;
        if (closed) return;
        lastResponse=System.nanoTime();
        var failure=new AtomicReference<Throwable>();
        Thread reader=Thread.ofVirtual().name("ToolVisuals media responses").start(()->{
            try (var stream=new BufferedReader(new InputStreamReader(child.getInputStream(),StandardCharsets.UTF_8.newDecoder()))) {
                String line;
                while (!closed && (line=MediaProtocol.readLine(stream))!=null) {
                    var event=MediaProtocol.decode(line,epoch,System.nanoTime());
                    lastResponse=System.nanoTime();
                    if (epoch!=generation || child!=process) break;
                    if (event instanceof MediaProtocol.Ready) { ready=true; listener.status(epoch,Status.READY); }
                    else if (!ready && !(event instanceof MediaProtocol.Failure)) throw new IOException("Media state received before handshake");
                    if (!closed) listener.event(epoch,event);
                }
                if (!closed) failure.compareAndSet(null,new IOException("Windows media helper closed its output"));
            } catch (Exception error) { if (!closed) failure.compareAndSet(null,error); }
        });
        Thread stderr=Thread.ofVirtual().name("ToolVisuals media errors").start(()->{
            try (var stream=new BufferedReader(new InputStreamReader(child.getErrorStream(),StandardCharsets.UTF_8))) {
                var text=new StringBuilder();
                for (int ch; (ch=stream.read())>=0;) {
                    if (text.length()<8192) text.append((char)ch);
                    else throw new IOException("Windows media helper error output limit exceeded");
                }
                if (!text.isEmpty() && !closed) errors.accept("Windows media helper stderr",new IOException(text.toString()));
            } catch (IOException error) { if (!closed) failure.compareAndSet(null,error); }
        });
        try (var writer=new BufferedWriter(new OutputStreamWriter(child.getOutputStream(),StandardCharsets.UTF_8))) {
            long heartbeat=0;
            while (!closed && child.isAlive()) {
                if (failure.get()!=null) throw new IOException("Cannot read Windows media sessions",failure.get());
                if (System.nanoTime()-lastResponse>TimeUnit.MILLISECONDS.toNanos(timing.timeoutMillis)) throw new IOException("Windows media helper timed out");
                if (System.nanoTime()>=heartbeat) {
                    writer.write("{\"action\":\"ping\"}\n"); writer.flush(); heartbeat=System.nanoTime()+TimeUnit.MILLISECONDS.toNanos(timing.heartbeatMillis);
                }
                var command=commands.poll(150,TimeUnit.MILLISECONDS);
                if (command!=null && command.generation==epoch && ready) { writer.write(command.line); writer.newLine(); writer.flush(); }
            }
            if (!closed) throw new IOException("Windows media helper exited with code "+child.exitValue(),failure.get());
        } finally {
            child.destroy(); reader.interrupt(); stderr.interrupt();
        }
    }
    private void terminate() {
        var child=process; process=null;
        if (child==null) return;
        child.destroy();
        try { if (!child.waitFor(1,TimeUnit.SECONDS)) child.destroyForcibly(); }
        catch (InterruptedException error) { child.destroyForcibly(); if (!closed) errors.accept("Stop Windows media helper",error); Thread.currentThread().interrupt(); }
    }
    @Override public void close() {
        closed=true; ready=false; commands.clear(); worker.interrupt();
        var child=process;
        if (child!=null) Thread.ofVirtual().name("ToolVisuals media shutdown").start(()->{
            try { child.destroy(); } catch (RuntimeException error) { errors.accept("Release Windows media helper",error); }
        });
    }
}
