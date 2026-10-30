package ru.he3hauka.toolvisuals.mixin.death;

import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.animation.death.SoulRenderState;

@Mixin(AvatarRenderer.class)
public abstract class SoulAvatarRendererMixin {
    @Inject(method="shouldRenderLayers(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Z",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$soulLayers(AvatarRenderState state,CallbackInfoReturnable<Boolean> cir){if(state instanceof SoulRenderState)cir.setReturnValue(false);}
}
