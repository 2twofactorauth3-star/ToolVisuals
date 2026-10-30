package com.viaversion.viafabricplus.injection.mixin.features.movement.collision;

import net.minecraft.block.BedBlock;
import net.minecraft.entity.Entity;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BedBlock.class)
public abstract class MixinBedBlock {

    @Inject(method = "bounceEntity", at = @At("HEAD"), cancellable = true)
    private void cancelEntityBounce(Entity entity, CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_11_1)) {
            ci.cancel();
        }
    }

}
