package ru.he3hauka.toolvisuals.client.hud.layout;

 
public final class HudPosition {
    public final float x, y;
    public HudPosition(float x, float y) {
        this.x = Float.isFinite(x) ? Math.clamp(x, 0, 100) : 50;
        this.y = Float.isFinite(y) ? Math.clamp(y, 0, 100) : 50;
    }
    public static HudPosition fromPixels(float x, float y, float screenWidth, float screenHeight, float width, float height) {
        return new HudPosition(screenWidth <= width ? 0 : x * 100 / (screenWidth - width),
                screenHeight <= height ? 0 : y * 100 / (screenHeight - height));
    }
}
