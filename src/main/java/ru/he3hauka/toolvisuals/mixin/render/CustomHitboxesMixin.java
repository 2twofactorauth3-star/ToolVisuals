package ru.he3hauka.toolvisuals.mixin.render;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.util.debug.DebugValueAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.CUSTOM_HITBOXES;

@Mixin(EntityHitboxDebugRenderer.class)
public abstract class CustomHitboxesMixin {
    @Inject(method="emitGizmos",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$replaceDebugHitboxes(double x,double y,double z,DebugValueAccess values,Frustum frustum,float partial,CallbackInfo ci) {
        if(ToolVisualsClient.CONFIG.active(CUSTOM_HITBOXES))ci.cancel();
    }
}
