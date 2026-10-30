package ru.he3hauka.toolvisuals.client.music.model;

import java.util.List;

public record MediaSnapshot(long generation, long received, String current, String pinned, List<MediaSession> sessions) {
    public static final MediaSnapshot EMPTY = new MediaSnapshot(0, 0, "", "", List.of());
    public MediaSnapshot { sessions = List.copyOf(sessions); }
    public MediaSession selected() {
        for(int index=0;index<sessions.size();index++) {var session=sessions.get(index);if(session.id().equals(current))return session;}
        return null;
    }
    public String artworkKey() {
        var session = selected();
        return session == null ? "" : generation + "/" + session.id() + "/" + session.track();
    }
}
