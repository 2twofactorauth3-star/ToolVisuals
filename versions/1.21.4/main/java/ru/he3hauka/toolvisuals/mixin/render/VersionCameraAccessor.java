package ru.he3hauka.toolvisuals.mixin.render;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface VersionCameraAccessor {
    @Invoker("getFov") float toolvisuals$fov(Camera camera,float partial,boolean world);
}
