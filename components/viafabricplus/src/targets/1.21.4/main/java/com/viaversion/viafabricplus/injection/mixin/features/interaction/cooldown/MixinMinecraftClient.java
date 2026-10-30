package com.viaversion.viafabricplus.injection.mixin.features.interaction.cooldown;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Shadow
    protected int attackCooldown;

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;attackCooldown:I", ordinal = 1))
    private int moveCooldownIncrement(MinecraftClient instance) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            return 0;
        } else {
            return attackCooldown;
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;handleInputEvents()V"))
    private void moveCooldownIncrement(CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            if (this.attackCooldown > 0) {
                --this.attackCooldown;
            }
        }
    }

    @WrapOperation(method = "doAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;hasLimitedAttackSpeed()Z", ordinal = 1))
    private boolean removeHitPenalty(ClientPlayerInteractionManager instance, Operation<Boolean> original) {

        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_7_6) && original.call(instance);
    }

}
