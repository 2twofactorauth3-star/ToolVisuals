package ru.he3hauka.toolvisuals.mixin.accessories;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.cosmetic.accessory.AccessoryClient;
import ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.*;

@Mixin(AvatarRenderer.class)
public abstract class AccessoryRendererMixin {
    @SuppressWarnings("unchecked")
    @Inject(method="<init>",at=@At("TAIL"))
    private void toolvisuals$layers(CallbackInfo ci){
        ((LivingRendererInvoker)(Object)this).toolvisuals$addLayer(new AccessoryLayer((RenderLayerParent<AvatarRenderState,PlayerModel>)(Object)this));
    }
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",at=@At("TAIL"))
    private void toolvisuals$appearance(Avatar player,AvatarRenderState state,float partial,CallbackInfo ci){
        ((AccessoryVisualState)state).toolvisuals$accessories(AccessoryClient.frame((Entity)(Object)player,state,partial));
    }
}
