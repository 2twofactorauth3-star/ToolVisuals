package ru.he3hauka.toolvisuals.mixin.optimization;

import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.optimization.occlusion.ExtractionVisibility;

@Mixin(AvatarRenderer.class)
public abstract class EarlyAvatarMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",shift=At.Shift.AFTER),cancellable=true)
    private void toolvisuals$skipHiddenLayers(Avatar entity,AvatarRenderState state,float partial,CallbackInfo callback) {
        if(ExtractionVisibility.hidden(entity)){state.isCrouching=entity.isCrouching();callback.cancel();}
    }
}
