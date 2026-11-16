package ru.he3hauka.toolvisuals.mixin.render;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.module.VisualModule;

@Mixin(EntityRenderDispatcher.class)
public abstract class CustomHitboxesMixin {
    @Inject(method="renderHitbox",at=@At("HEAD"),cancellable=true)
    private static void toolvisuals$replaceVanillaDebugBoxes(CallbackInfo callback){if(ToolVisualsClient.CONFIG.active(VisualModule.CUSTOM_HITBOXES))callback.cancel();}
}
