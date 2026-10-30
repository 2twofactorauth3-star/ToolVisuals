package ru.he3hauka.toolvisuals.client.replay.playback;

import java.io.IOException;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import ru.he3hauka.toolvisuals.client.replay.io.*;

public final class ReplayPlayback implements AutoCloseable {
    public final ReplaySnapshot snapshot;
    public final ReplayClock clock = new ReplayClock();
    private final Screen screen;
    private ReplayScene scene;
    private ReplayStream stream;
    private ReplayFrame pending;
    private long ticks, actual, target;
    private boolean seeking, closed;

    public ReplayPlayback(ReplaySnapshot snapshot, Screen screen, long start) throws IOException {
        var version = SharedConstants.getCurrentVersion();
        if (snapshot.header.protocol != version.protocolVersion() || !snapshot.header.gameVersion.equals(version.name()))
            throw new IOException("Replay belongs to a different Minecraft version");
        this.snapshot = snapshot; this.screen = screen;
        target=Math.clamp(start,0,snapshot.durationMillis);seeking=target>0;
        restart(); clock.seek(target,System.nanoTime());clock.play(true, System.nanoTime());
    }

    public long position() { return actual; }
    public boolean seeking() { return seeking; }
    public boolean ready() { return scene != null && scene.ready(); }
    public void toggle() { clock.play(!clock.playing(), System.nanoTime()); }
    public void speed(double value) { clock.speed(value, System.nanoTime()); }
    public void follow() { if (scene != null) scene.follow(); }
    public void move(double forward, double side, double vertical, float yaw, float pitch) {
        if (scene != null && !seeking) scene.move(forward, side, vertical, yaw, pitch);
    }

    public void seek(long millis) {
        target = Math.clamp(millis, 0, snapshot.durationMillis); seeking = true;
        clock.seek(target, System.nanoTime()); restart();
    }

    private void restart() {
        if (stream != null) stream.close();
        if (scene != null) scene.close();
        pending = null; ticks = 0; actual = 0;
        scene = new ReplayScene(snapshot.header); scene.screen(screen); stream = new ReplayStream(snapshot);
    }

    public void update() throws IOException {
        if (closed) return;
        long now = System.nanoTime();
        long wanted = seeking ? target : Math.min(snapshot.durationMillis, (long)clock.advance(now));
        long deadline = now + 6_000_000;
        int work = 0;
        while (work++ < 512 && System.nanoTime() < deadline) {
            if (stream.failure() != null) throw new IOException("Cannot read replay recording", stream.failure());
            if (pending == null) pending = stream.poll();
            if (pending == null) {
                if(stream.ended()&&!scene.ready())throw new IOException("Replay contains no playable world");
                if (stream.ended()) finishSeeking(wanted, now);
                else if (!seeking) clock.seek(actual, now);
                break;
            }
            if (pending.millis > wanted) { finishSeeking(wanted, now); break; }
            if (!seeking && ticks + 50 <= pending.millis) { scene.tick(); ticks += 50; actual = ticks; continue; }
            scene.apply(pending); actual = pending.millis;
            if (seeking) ticks = actual - actual % 50;
            pending = null;
        }
        if (actual >= snapshot.durationMillis) clock.play(false, now);
    }

    private void finishSeeking(long wanted, long now) {
        actual = wanted;
        if (seeking) { scene.snapInterpolations(); scene.follow(); seeking = false; clock.seek(wanted, now); }
    }

    public void render(Runnable action) {
        if (scene != null && scene.ready()) {
            scene.resize(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());
            scene.position(actual);
            scene.render(action);
        } else action.run();
    }

    @Override public void close() { if (!closed) { closed = true; if (stream != null) stream.close(); if (scene != null) scene.close(); } }
}
