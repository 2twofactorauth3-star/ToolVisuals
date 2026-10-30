package com.viaversion.viafabricplus.injection.mixin.features.interaction.cooldown;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayer {

    @Redirect(method = "getAttackStrengthScale", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
    private float removeAttackCooldown(final float value, final float min, final float max) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            return 1.0F;
        } else {
            return Mth.clamp(value, min, max);
        }
    }

    @Redirect(method = "getItemSwapScale", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
    private float removeSwapCooldown(final float value, final float min, final float max) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            return 1.0F;
        } else {
            return Mth.clamp(value, min, max);
        }
    }

}
