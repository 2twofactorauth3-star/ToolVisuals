package ru.he3hauka.toolvisuals.client.hud;

public final class HudFormats {
    private HudFormats() {}
    public static String duration(int ticks, boolean infinite) {
        if (infinite) return "\u221e";
        int seconds = Math.max(0, ticks / 20);
        return String.format(java.util.Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
    }
    public static float position(float available, float size, float percent) {
        return Math.max(0, available - size) * Math.clamp(percent, 0, 100) / 100;
    }
}
