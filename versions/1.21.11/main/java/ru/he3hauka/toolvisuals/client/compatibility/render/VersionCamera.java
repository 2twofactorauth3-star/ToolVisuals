package ru.he3hauka.toolvisuals.client.compatibility.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import ru.he3hauka.toolvisuals.mixin.render.VersionCameraAccessor;

public final class VersionCamera {
    private static Matrix4f projection=new Matrix4f(),view=new Matrix4f();
    private static Frustum frustum;
    private VersionCamera() {}
    public static void capture(Matrix4f nextView,Matrix4f nextProjection) {
        view.set(nextView);projection.set(nextProjection);frustum=new Frustum(view,projection);
        var position=Minecraft.getInstance().gameRenderer.getMainCamera().position();frustum.prepare(position.x,position.y,position.z);
    }
    public static Matrix4f projection() {return projection;}
    public static Matrix4f view() {return view;}
    public static Frustum frustum() {return frustum;}
    public static float handFov() {
        var client=Minecraft.getInstance();return ((VersionCameraAccessor)client.gameRenderer).toolvisuals$fov(client.gameRenderer.getMainCamera(),client.getDeltaTracker().getGameTimeDeltaPartialTick(false),false);
    }
}
