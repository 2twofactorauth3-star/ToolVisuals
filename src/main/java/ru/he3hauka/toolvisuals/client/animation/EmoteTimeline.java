package ru.he3hauka.toolvisuals.client.animation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import ru.he3hauka.toolvisuals.animation.Emote;
import ru.he3hauka.toolvisuals.animation.PairEmote;

public final class EmoteTimeline {
    public static final double TRANSITION_TICKS = 6;
    public record Playback(Emote emote, double started, PairEmote pair, boolean second) {
        public Playback(Emote emote, double started) { this(emote, started, null, false); }
    }

    private static final class Track {
        final Playback playback;
        final EmotePose from;
        final EmotePose.Rotation rootOffset;
        final double changed;

        Track(Playback playback, EmotePose from, EmotePose target, double changed) {
            this.playback = playback;
            this.from = from;
            this.changed = changed;
            rootOffset = from == null ? EmotePose.Rotation.ZERO : EmotePose.rootOffset(from, target);
        }
    }

    private final Map<UUID, Track> playing = new LinkedHashMap<>();
    private UUID owner;

    public void protect(UUID player) { owner = player; }

    public void start(UUID player, Emote emote, double clock, double age) {
        start(player, emote, null, false, clock, age);
    }

    public void startPair(UUID player, PairEmote emote, boolean second, double clock, double age) {
        if (emote != null) start(player, emote.animation, emote, second, clock, age);
    }

    private void start(UUID player, Emote emote, PairEmote pair, boolean second, double clock, double age) {
        if (player == null || emote == null || !Double.isFinite(clock)) return;
        double elapsed = Math.clamp(age, 0, emote.duration);
        var previous = pose(player, clock);
        if (previous == null && elapsed > 0) previous = EmotePose.NEUTRAL;
        var playback = new Playback(emote, clock - elapsed, pair, second);
        playing.put(player, new Track(playback, previous, sample(playback, elapsed), clock));
        if (playing.size() > 256) {
            var iterator = playing.keySet().iterator();
            while (iterator.hasNext()) if (!iterator.next().equals(owner)) { iterator.remove(); break; }
        }
    }

    public void stop(UUID player, double clock) {
        var track = playing.get(player);
        if (track == null || track.playback == null) return;
        var previous = pose(track, clock);
        if (previous == null) { playing.remove(player); return; }
        playing.put(player, new Track(null, previous, EmotePose.NEUTRAL, clock));
    }

    public void remove(UUID player) { playing.remove(player); }

    public Playback current(UUID player, double clock) {
        var track = playing.get(player);
        if (track == null || track.playback == null || !Double.isFinite(clock)) return null;
        double age = clock - track.playback.started;
        return age >= 0 && age < track.playback.emote.duration ? track.playback : null;
    }

    public EmotePose pose(UUID player, double clock) { return pose(playing.get(player), clock); }

    private static EmotePose pose(Track track, double clock) {
        if (track == null || !Double.isFinite(clock) || clock < track.changed) return null;
        double blend = (clock - track.changed) / TRANSITION_TICKS;
        if (track.playback == null) return blend >= 1 ? null : EmotePose.mix(track.from, EmotePose.NEUTRAL, blend, track.rootOffset);
        double age = clock - track.playback.started;
        if (age < 0 || age >= track.playback.emote.duration) return null;
        var target = sample(track.playback, age);
        return track.from == null || blend >= 1 ? target : EmotePose.mix(track.from, target, blend, track.rootOffset);
    }

    private static EmotePose sample(Playback playback, double age) { return playback.pair == null ? EmotePose.sample(playback.emote, age) : EmotePose.samplePair(playback.pair, age, playback.second); }

    public void expire(double clock) {
        playing.values().removeIf(track -> !Double.isFinite(clock) || clock < track.changed
                || (track.playback == null ? clock - track.changed >= TRANSITION_TICKS : clock - track.playback.started >= track.playback.emote.duration));
    }

    public void clear() { playing.clear(); }
    public int size() { return playing.size(); }
}
