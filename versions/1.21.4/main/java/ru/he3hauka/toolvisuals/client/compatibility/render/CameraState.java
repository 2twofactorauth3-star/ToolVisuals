package ru.he3hauka.toolvisuals.client.compatibility.render;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public final class CameraState {
    public Vec3 pos=Vec3.ZERO;
    public final Quaternionf orientation=new Quaternionf();
    public Matrix4f projectionMatrix=new Matrix4f(),viewRotationMatrix=new Matrix4f();
    public boolean isFirstPerson;
    public Frustum cullFrustum;
}
