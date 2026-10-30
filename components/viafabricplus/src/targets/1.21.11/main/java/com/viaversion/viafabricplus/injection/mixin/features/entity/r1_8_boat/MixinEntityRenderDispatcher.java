package com.viaversion.viafabricplus.injection.mixin.features.entity.r1_8_boat;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.server.packs.resources.ResourceManager;

import com.llamalad7.mixinextras.sugar.Local;
import com.viaversion.viafabricplus.features.entity.r1_8_boat.BoatRenderer1_8;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRenderDispatcher {

    @Unique
    private BoatRenderer1_8 viaFabricPlus$boatRenderer;

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void createBoatRenderer1_8(ResourceManager manager, CallbackInfo ci, @Local EntityRendererProvider.Context context) {
        viaFabricPlus$boatRenderer = new BoatRenderer1_8(context);
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "getRenderer(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)Lnet/minecraft/client/renderer/entity/EntityRenderer;", at = @At("HEAD"), cancellable = true)
    private <S extends EntityRenderState> void useBoatRenderer1_8(S state, CallbackInfoReturnable<EntityRenderer<?, ? super S>> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8) && state instanceof BoatRenderState) {
            cir.setReturnValue((EntityRenderer<?, ? super S>) viaFabricPlus$boatRenderer);
        }
    }

}
