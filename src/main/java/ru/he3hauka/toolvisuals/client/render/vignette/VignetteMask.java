package ru.he3hauka.toolvisuals.client.render.vignette;

public final class VignetteMask {
    private VignetteMask() {}

    public static double opacity(double u,double v,double width,double softness) {
        if (!Double.isFinite(u)||!Double.isFinite(v)||!Double.isFinite(width)||!Double.isFinite(softness)) return 0;
        double x=Math.abs(u*2-1),y=Math.abs(v*2-1);
        double radius=Math.sqrt(Math.sqrt(x*x*x*x+y*y*y*y));
        double t=Math.clamp((radius-1+Math.clamp(width,.1,.65))/Math.clamp(width,.1,.65),0,1);
        return Math.pow(t*t*(3-2*t),3-2*Math.clamp(softness,.1,1));
    }
}
