package ru.he3hauka.toolvisuals.client.sound.library;

import java.util.Locale;
import java.util.Set;
import ru.he3hauka.toolvisuals.client.sound.SoundPreset;

public final class SoundIds {
    private static final Set<String> DEVICES = Set.of("con", "prn", "aux", "nul", "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9", "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9");
    private SoundIds() {}
    public static String normalize(String id) { return id == null ? "" : id.strip().toLowerCase(Locale.ROOT); }
    public static boolean valid(String id) { return id != null && id.matches("[a-z][a-z0-9_-]{0,31}") && !DEVICES.contains(id) && !SoundPreset.reserved(id); }
    public static String require(String input) { String id = normalize(input); if (!valid(id)) throw new IllegalArgumentException("Invalid or reserved sound ID"); return id; }
}
