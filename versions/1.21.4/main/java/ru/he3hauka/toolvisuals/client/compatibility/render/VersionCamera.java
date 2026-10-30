package ru.he3hauka.toolvisuals.client.compatibility.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;

public final class VersionCamera {
    public static final SceneState SCENE=new SceneState();
    private VersionCamera() {}
    public static Matrix4f projection(){return SCENE.cameraRenderState.projectionMatrix;}
    public static Matrix4f view(){return SCENE.cameraRenderState.viewRotationMatrix;}
    public static Frustum frustum(){return SCENE.cameraRenderState.cullFrustum;}
    public static float handFov(){var client=Minecraft.getInstance();return ((ru.he3hauka.toolvisuals.mixin.render.VersionCameraAccessor)client.gameRenderer).toolvisuals$fov(client.gameRenderer.getMainCamera(),SCENE.worldPartialTicks,false);}
    public static void capture(Matrix4f view,Matrix4f projection,Frustum frustum,float partial){var client=Minecraft.getInstance();var camera=client.gameRenderer.getMainCamera();var state=SCENE.cameraRenderState;state.pos=camera.getPosition();state.orientation.set(camera.rotation());state.viewRotationMatrix.set(view);state.projectionMatrix.set(projection);state.cullFrustum=frustum;state.isFirstPerson=client.options.getCameraType().isFirstPerson();SCENE.worldPartialTicks=partial;SCENE.gameTime=client.level==null?0:client.level.getGameTime();SCENE.entityRenderStates.clear();}
}
