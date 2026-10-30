package com.viaversion.viafabricplus.injection.mixin.features.v26_2.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class MixinPlayer {

    @ModifyExpressionValue(method = "getDestroySpeed", at = @At(value = "INVOKE", target = "Ljava/lang/Math;pow(DD)D"))
    private double changeMiningFatigueCalculation(double original, @Local(name = "amplifier") int amplifier) {
        if (ViaFabricPlus.api().targetVersion().olderThanOrEqualTo(ProtocolVersion.v26_2)) {
            return switch (amplifier) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };
        } else {
            return original;
        }
    }

    @Redirect(method = "tryToStartFallFlying", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInLiquid()Z"))
    private boolean allowFallFlyingInLava(Player instance) {
        if (ViaFabricPlus.api().targetVersion().olderThanOrEqualTo(ProtocolVersion.v26_2)) {
            return instance.isInWater();
        } else {
            return instance.isInLiquid();
        }
    }

}
