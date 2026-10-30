package com.viaversion.viafabricplus.injection.mixin.features.entity.r1_8_boat;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.resource.ResourceManager;

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

    @Inject(method = "reload", at = @At("TAIL"))
    private void createBoatRenderer1_8(ResourceManager manager, CallbackInfo ci, @Local EntityRendererFactory.Context context) {
        viaFabricPlus$boatRenderer = new BoatRenderer1_8(context);
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "getRenderer", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void useBoatRenderer1_8(T entity, CallbackInfoReturnable<EntityRenderer<? super T, ?>> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8) && entity instanceof BoatEntity) {
            cir.setReturnValue((EntityRenderer<? super T, ?>) viaFabricPlus$boatRenderer);
        }
    }

}
