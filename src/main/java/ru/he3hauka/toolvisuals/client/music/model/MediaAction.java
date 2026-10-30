package ru.he3hauka.toolvisuals.client.music.model;

public enum MediaAction {
    TOGGLE("toggle"), PREVIOUS("previous"), NEXT("next"), STOP("stop"),
    SEEK("seek"), SHUFFLE("shuffle"), REPEAT("repeat"), RATE("rate"), SELECT("select"), FORWARD("forward"), REWIND("rewind"),
    VOLUME("volume"), MUTE("mute");

    public final String wire;
    MediaAction(String wire) { this.wire = wire; }
    public boolean controlsVolume() { return this==VOLUME||this==MUTE; }
    public boolean available(MediaSession session) {
        if (this == SELECT) return true;
        if (session == null) return false;
        return switch (this) {
            case TOGGLE -> session.has(MediaSession.TOGGLE) || session.has(session.playing() ? MediaSession.PAUSE : MediaSession.PLAY);
            case PREVIOUS -> session.has(MediaSession.PREVIOUS | MediaSession.CHANNEL_DOWN);
            case NEXT -> session.has(MediaSession.NEXT | MediaSession.CHANNEL_UP);
            case STOP -> session.has(MediaSession.STOP);
            case SEEK -> session.has(MediaSession.SEEK);
            case SHUFFLE -> session.has(MediaSession.SHUFFLE);
            case REPEAT -> session.has(MediaSession.REPEAT);
            case RATE -> session.has(MediaSession.RATE);
            case FORWARD -> session.has(MediaSession.FORWARD);
            case REWIND -> session.has(MediaSession.REWIND);
            case VOLUME -> session.has(MediaSession.VOLUME)&&session.volume().available();
            case MUTE -> session.has(MediaSession.MUTE)&&session.volume().available();
            case SELECT -> true;
        };
    }
}
