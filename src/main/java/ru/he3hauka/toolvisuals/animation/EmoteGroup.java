package ru.he3hauka.toolvisuals.animation;

import java.util.List;
import java.util.Locale;

public enum EmoteGroup {
    SOCIAL, JOY, AFFECTION, MOOD, TRICKS;

    public final String id = name().toLowerCase(Locale.ROOT);
    public List<Emote> emotes() { return Emote.in(this); }
}
