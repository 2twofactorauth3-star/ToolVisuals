package ru.he3hauka.toolvisuals.client.render.motionblur;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;

public final class MotionHistory {
    private Object context, world;
    private int width, height;
    private long previous;
    private double x, y, z;
    private boolean initialized;
    private final Matrix4f projection=new Matrix4f(),view=new Matrix4f();
    private final Quaternionf previousRotation=new Quaternionf(),currentRotation=new Quaternionf();

    public record Sample(Matrix4f clipToPrevious,float exposureScale,float maximumPixels,boolean reset) {}

    public Sample sample(Object context,Object world,int width,int height,double x,double y,double z,
                         Matrix4fc projection,Matrix4fc view,double strength,long now) {
        if(width<=0||height<=0||!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)
                ||!projection.isFinite()||!view.isFinite()||Math.abs(projection.determinant())<1e-12||Math.abs(view.determinant())<1e-12) {
            clear();return new Sample(new Matrix4f(),0,0,true);
        }
        boolean reset = !initialized || this.context != context || this.world != world || this.width != width || this.height != height
                || now <= previous || now - previous > 120000000 || squared(x - this.x, y - this.y, z - this.z) > 16 * 16;
        if(!reset) {
            this.view.getNormalizedRotation(previousRotation);view.getNormalizedRotation(currentRotation);
            double fovRatio=projection.m11()/this.projection.m11();
            reset=Math.abs(previousRotation.dot(currentRotation))<.8660254||fovRatio<.8||fovRatio>1.25;
        }
        double elapsed = initialized && now > previous ? (now - previous) / 1000000000d : 0;
        double amount=Double.isFinite(strength)?Math.clamp(strength,0,1):0;
        var transform=new Matrix4f();
        if(!reset) {
            var inverse=new Matrix4f(projection).mul(view).invert();
            transform.set(this.projection).mul(this.view).translate((float)(x-this.x),(float)(y-this.y),(float)(z-this.z)).mul(inverse);
            if(!transform.isFinite()){transform.identity();reset=true;}
        }
        float exposure=reset||amount==0?0:(float)Math.min(32,amount/(30*elapsed));
        float maximum=(float)Math.clamp(height/1080d*28,4,56);
        this.context = context; this.world = world; this.width = width; this.height = height; previous = now;
        this.x = x; this.y = y; this.z = z;this.projection.set(projection);this.view.set(view);initialized = true;
        return new Sample(transform,exposure,maximum,reset);
    }

    private static double squared(double x, double y, double z) { return x * x + y * y + z * z; }
    public void clear() { initialized = false; context = world = null; }
}
