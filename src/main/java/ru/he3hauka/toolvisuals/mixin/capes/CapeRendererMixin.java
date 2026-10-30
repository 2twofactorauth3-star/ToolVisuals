package ru.he3hauka.toolvisuals.mixin.capes;

import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.cape.CapeClient;

@Mixin(AvatarRenderer.class)
public abstract class CapeRendererMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",at=@At("TAIL"))
    private void toolvisuals$cape(Avatar player,AvatarRenderState state,float partial,CallbackInfo ci){CapeClient.apply(player.getUUID(),state);}
}
