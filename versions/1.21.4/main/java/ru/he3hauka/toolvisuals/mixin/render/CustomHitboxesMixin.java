package ru.he3hauka.toolvisuals.mixin.render;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class CustomHitboxesMixin {
    @Inject(method="renderHitbox",at=@At("HEAD"),cancellable=true)
    private static void toolvisuals$replaceVanillaDebugBoxes(CallbackInfo callback){if(ru.he3hauka.toolvisuals.client.ToolVisualsClient.CONFIG.active(ru.he3hauka.toolvisuals.client.module.VisualModule.CUSTOM_HITBOXES))callback.cancel();}
}
