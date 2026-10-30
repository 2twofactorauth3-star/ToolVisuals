package com.viaversion.viafabricplus.injection.mixin.features.entity.interaction;

import net.minecraft.world.entity.animal.squid.Squid;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Squid.class)
public abstract class MixinSquid {

    @Inject(method = "canBeLeashed", at = @At("HEAD"), cancellable = true)
    private void cancelLeashing(CallbackInfoReturnable<Boolean> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_16_4)) {
            cir.setReturnValue(false);
        }
    }

}
