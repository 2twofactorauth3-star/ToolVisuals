package ru.he3hauka.toolvisuals.client.compatibility.render;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public final class SceneState {
    public final CameraState cameraRenderState=new CameraState();
    public final List<EntityRenderState> entityRenderStates=new ArrayList<>();
    public long gameTime;
    public float worldPartialTicks;
}
