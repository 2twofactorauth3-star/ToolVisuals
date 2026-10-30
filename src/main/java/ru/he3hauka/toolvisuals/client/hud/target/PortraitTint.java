package ru.he3hauka.toolvisuals.client.hud.target;

public final class PortraitTint {
    private PortraitTint() {}
    public static int color(float opacity,boolean premultiplied){
        int alpha=Math.round(Math.clamp(Float.isFinite(opacity)?opacity:0,0,1)*255);
        return alpha<<24|(premultiplied?alpha<<16|alpha<<8|alpha:0xffffff);
    }
}
