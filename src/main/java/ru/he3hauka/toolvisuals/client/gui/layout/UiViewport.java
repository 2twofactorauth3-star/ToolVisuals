package ru.he3hauka.toolvisuals.client.gui.layout;

 
public final class UiViewport {
    public final float scale;
    public final int width;
    public final int height;

    public UiViewport(int screenWidth, int screenHeight, int gameScale) {
        float density = Math.clamp(screenHeight * gameScale / 720f, 1f, 2f);
        scale = density / gameScale;
        width = Math.round(screenWidth / scale);
        height = Math.round(screenHeight / scale);
    }
}
