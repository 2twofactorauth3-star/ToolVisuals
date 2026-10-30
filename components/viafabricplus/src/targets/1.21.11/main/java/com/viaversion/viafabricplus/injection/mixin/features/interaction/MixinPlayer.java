package com.viaversion.viafabricplus.injection.mixin.features.interaction;

import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayer {

    @Shadow
    @Final
    private Abilities abilities;

    @Inject(method = "canEat", at = @At("HEAD"), cancellable = true)
    private void preventEatingFoodInCreative(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_4) && this.abilities.invulnerable) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isCreative", at = @At("HEAD"), cancellable = true)
    private void fixCreativeCheck(CallbackInfoReturnable<Boolean> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            cir.setReturnValue(this.abilities.instabuild);
        }
    }

}
