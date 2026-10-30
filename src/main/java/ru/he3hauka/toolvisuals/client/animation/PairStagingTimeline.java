package ru.he3hauka.toolvisuals.client.animation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class PairStagingTimeline {
    public static final double APPROACH_TICKS = 10;
    public record Frame(Vec3 offset, float yaw, float weight) {}
    private static final class Track {
        final Vec3 target, from;
        final float targetYaw, fromYaw;
        final double changed, end;
        double stopped = Double.NaN;
        Frame last, stopFrame;
        Track(Vec3 target, float targetYaw, Frame from, double clock, double end) {
            this.target = target; this.targetYaw = targetYaw; this.from = from == null ? Vec3.ZERO : from.offset;
            fromYaw = from == null ? Float.NaN : from.yaw; changed = clock; this.end = end;
        }
    }
    private final Map<UUID, Track> tracks = new LinkedHashMap<>();

    public void start(UUID actor, Vec3 actual, float originalYaw, Vec3 target, float targetYaw, double clock, double remaining) {
        if (actor == null || !PairStage.finite(actual) || !PairStage.finite(target) || !Float.isFinite(originalYaw) || !Float.isFinite(targetYaw) || !Double.isFinite(clock) || !Double.isFinite(remaining) || remaining <= 0) return;
        var previous = frame(actor, actual, originalYaw, clock);
        tracks.put(actor, new Track(target, targetYaw, previous, clock, clock + remaining));
        if (tracks.size() > 256) tracks.remove(tracks.keySet().iterator().next());
    }

    public Frame frame(UUID actor, Vec3 actual, float originalYaw, double clock) {
        var track = tracks.get(actor);
        if (track == null || !Double.isFinite(clock) || clock < track.changed || !PairStage.finite(actual) || !Float.isFinite(originalYaw)) return null;
        Frame frame;
        if (Double.isFinite(track.stopped)) {
            double progress = (clock - track.stopped) / EmoteTimeline.TRANSITION_TICKS;
            if (progress >= 1 || track.stopFrame == null) return null;
            float fade = (float) EmotePose.ease(progress);
            frame = new Frame(track.stopFrame.offset.scale(1 - fade), turn(track.stopFrame.yaw, originalYaw, fade), track.stopFrame.weight * (1 - fade));
        } else {
            if (clock >= track.end) return null;
            float approach = (float) EmotePose.ease((clock - track.changed) / APPROACH_TICKS);
            float exit = (float) EmotePose.ease((track.end - clock) / APPROACH_TICKS);
            var destination = track.target.subtract(actual).scale(exit);
            var offset = track.from.scale(1 - approach).add(destination.scale(approach));
            float yaw = turn(Float.isFinite(track.fromYaw) ? track.fromYaw : originalYaw, turn(originalYaw, track.targetYaw, exit), approach);
            frame = new Frame(offset, yaw, approach * exit);
        }
        track.last = frame;
        return frame;
    }

    public void stop(UUID actor, double clock) {
        var track = tracks.get(actor);
        if (track == null || Double.isFinite(track.stopped) || !Double.isFinite(clock)) return;
        track.stopped = clock; track.stopFrame = track.last;
    }
    public void remove(UUID actor) { tracks.remove(actor); }
    public void clear() { tracks.clear(); }
    public int size() { return tracks.size(); }
    public void expire(double clock) { tracks.values().removeIf(track -> !Double.isFinite(clock) || clock < track.changed || (Double.isFinite(track.stopped) ? clock - track.stopped >= EmoteTimeline.TRANSITION_TICKS : clock >= track.end)); }
    private static float turn(float from, float to, float progress) { return from + Mth.wrapDegrees(to - from) * progress; }
}
