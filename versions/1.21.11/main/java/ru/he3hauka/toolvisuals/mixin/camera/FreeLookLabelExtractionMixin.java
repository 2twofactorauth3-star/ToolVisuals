package ru.he3hauka.toolvisuals.mixin.camera;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.camera.FreeLook;

@Mixin(EntityRenderer.class)
public abstract class FreeLookLabelExtractionMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V", at = @At("RETURN"))
    private void toolvisuals$hideOwnLabels(Entity entity, EntityRenderState state, float partialTick,
            CallbackInfo ci) {
        if (FreeLook.owns(entity) && (!FreeLook.showsOwnLabels(entity) || state.nameTag == null)) {
            state.nameTag = null; ru.he3hauka.toolvisuals.client.compatibility.render.VersionState.clearScore(state); state.nameTagAttachment = null;
        }
    }
}
