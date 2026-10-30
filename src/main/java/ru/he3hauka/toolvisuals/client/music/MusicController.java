package ru.he3hauka.toolvisuals.client.music;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import ru.he3hauka.toolvisuals.client.music.backend.MediaBridge;
import ru.he3hauka.toolvisuals.client.music.backend.MediaProtocol;
import ru.he3hauka.toolvisuals.client.music.model.MediaAction;
import ru.he3hauka.toolvisuals.client.music.model.MediaSession;
import ru.he3hauka.toolvisuals.client.music.model.MediaSnapshot;

public final class MusicController implements AutoCloseable, MediaBridge.Listener {
    public enum Status { STARTING, READY, UNAVAILABLE, CLOSED }
    public record Artwork(long generation,MediaProtocol.Artwork image) {}
    public record Feedback(String key,long until) {}
    private record Pending(long generation,long until) {}
    private final MediaBridge bridge;
    private final BiConsumer<String,Throwable> errors;
    private final java.util.function.LongSupplier clock;
    private final Map<Long,Pending> pending=new LinkedHashMap<>();
    private final AtomicReference<Artwork> artwork=new AtomicReference<>();
    private volatile MediaSnapshot snapshot=MediaSnapshot.EMPTY;
    private volatile Status status=Status.STARTING;
    private volatile Feedback feedback=new Feedback("",0);
    private boolean closed;
    private long sequence,lastControl,generation;

    public MusicController(MediaBridge.Launcher launcher,BiConsumer<String,Throwable> errors) {
        this(launcher,errors,System::nanoTime);
    }
    public MusicController(MediaBridge.Launcher launcher,BiConsumer<String,Throwable> errors,java.util.function.LongSupplier clock) {
        this.errors=errors;this.clock=clock;bridge=new MediaBridge(launcher,this,errors);
    }
    public void start() { bridge.start(); }
    public MediaSnapshot snapshot() { return snapshot; }
    public Status status() { return status; }
    public Artwork pollArtwork() { return artwork.getAndSet(null); }
    public String feedback(long now) { return now<feedback.until ? feedback.key : ""; }
    public synchronized void expire(long now) {
        if (pending.values().removeIf(request->now>=request.until)) feedback("timeout",now);
    }
    public synchronized boolean command(MediaSnapshot captured,MediaSession session,MediaAction action,double value) {
        long now=clock.getAsLong(); expire(now);
        if (closed || status!=Status.READY || captured.generation()!=generation) return reject("stale",now);
        if (!action.available(session)) return reject("unsupported",now);
        if (!Double.isFinite(value)) return reject("invalid",now);
        if (action.controlsVolume() && (value<0||value>1||action==MediaAction.MUTE&&value!=0&&value!=1)) return reject("invalid",now);
        String id=session==null?"":session.id(), track=session==null?"":session.track();
        if (!id.isEmpty()) {
            var actual=snapshot.sessions().stream().filter(item->item.id().equals(id)).findFirst().orElse(null);
            if (actual==null || action==MediaAction.SEEK && !actual.track().equals(track)) return reject("stale",now);
            if (action.controlsVolume() && !actual.volume().target().equals(session.volume().target())) return reject("stale",now);
        }
        if (pending.size()>=4 || now-lastControl<180_000_000L) return reject("busy",now);
        long request=++sequence;
        if (!bridge.send(generation,request,action,id,track,session==null?"":session.volume().target(),value)) return reject("busy",now);
        pending.put(request,new Pending(generation,now+10_000_000_000L)); lastControl=now; feedback=new Feedback("",0); return true;
    }
    private boolean reject(String reason,long now) { feedback(reason,now); return false; }
    private void feedback(String key,long now) { feedback=new Feedback(key,now+4_000_000_000L); }
    @Override public synchronized void status(long epoch,MediaBridge.Status state) {
        if (closed || epoch<generation) return;
        generation=epoch; status=Status.valueOf(state.name());
        if (state!=MediaBridge.Status.READY) { snapshot=MediaSnapshot.EMPTY; artwork.set(null); pending.clear(); }
    }
    @Override public void event(long epoch,MediaProtocol.Event event) {
        if (event instanceof MediaProtocol.Failure failure) {
            synchronized(this) { if(closed || epoch!=generation) return; }
            errors.accept("Windows media "+failure.operation(),new IOException("HRESULT 0x"+Long.toHexString(failure.code()&0xffffffffL)+": "+failure.message()));
            return;
        }
        synchronized(this) {
            if (closed || epoch!=generation) return;
            switch (event) {
            case MediaProtocol.State state -> snapshot=state.snapshot();
            case MediaProtocol.Artwork image -> { if (image.key(epoch).equals(snapshot.artworkKey())) artwork.set(new Artwork(epoch,image)); }
            case MediaProtocol.Reply reply -> {
                var request=pending.remove(reply.request());
                if (request!=null && request.generation==epoch) {
                    String key=switch (reply.status()) { case "ok"->""; case "stale","unsupported","rejected","busy","invalid"->reply.status(); default->"failed"; };
                    feedback(key,clock.getAsLong());
                }
            }
            case MediaProtocol.Failure failure -> throw new IllegalStateException("Media failure must be reported outside the state lock");
            case MediaProtocol.Ready ready -> {}
            }
        }
    }
    @Override public synchronized void close() {
        closed=true; status=Status.CLOSED; snapshot=MediaSnapshot.EMPTY; pending.clear(); artwork.set(null); bridge.close();
    }
}
