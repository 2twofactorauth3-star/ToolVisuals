package ru.he3hauka.toolvisuals.client.gui.render;

public final class UiClip {
    private UiClip() {}
    public static boolean empty(int left, int top, int right, int bottom, int scale, int width, int height) {
        return Math.min((long)right * scale, width) <= Math.max(0L, (long)left * scale)
                || Math.min((long)bottom * scale, height) <= Math.max(0L, (long)top * scale);
    }
}
