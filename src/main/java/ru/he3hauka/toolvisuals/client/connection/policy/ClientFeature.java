package ru.he3hauka.toolvisuals.client.connection.policy;

import java.util.Locale;

public enum ClientFeature {
    FRIENDS, GPS, HUD_EDITOR, CHAT_RESIZE, OUTFITS;

    public final String id = name().toLowerCase(Locale.ROOT);
}
