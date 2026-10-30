package ru.he3hauka.toolvisuals.animation;

import java.util.List;

public enum EmoteGroup {
    SOCIAL, JOY, AFFECTION, MOOD, TRICKS;

    public final String id = name().toLowerCase(java.util.Locale.ROOT);
    public List<Emote> emotes() { return Emote.in(this); }
}
