package com.viaversion.viafabricplus.injection.mixin.features.movement.liquid;

import net.minecraft.world.entity.item.ItemEntity;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity {

    @Inject(method = "setUnderwaterMovement", at = @At("HEAD"), cancellable = true)
    private void dontApplyWaterBuoyancy(CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            ci.cancel();
        }
    }

}
