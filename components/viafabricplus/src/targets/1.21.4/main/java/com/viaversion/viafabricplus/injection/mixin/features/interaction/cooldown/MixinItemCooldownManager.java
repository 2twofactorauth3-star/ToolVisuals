package com.viaversion.viafabricplus.injection.mixin.features.interaction.cooldown;

import net.minecraft.entity.player.ItemCooldownManager;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemCooldownManager.class)
public abstract class MixinItemCooldownManager {

    @Inject(method = "set(Lnet/minecraft/util/Identifier;I)V", at = @At("HEAD"), cancellable = true)
    private void dontSetCooldown(CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            ci.cancel();
        }
    }

}
