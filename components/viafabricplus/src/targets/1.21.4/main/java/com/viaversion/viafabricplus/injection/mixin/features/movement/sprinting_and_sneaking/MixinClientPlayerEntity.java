package com.viaversion.viafabricplus.injection.mixin.features.movement.sprinting_and_sneaking;

import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.authlib.GameProfile;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPlayerEntity.class, priority = 2000)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

    @Shadow
    protected abstract boolean shouldStopSprinting();

    @Shadow
    protected abstract void sendSneakingPacket();

    @Shadow
    private boolean inSneakingPose;

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;shouldStopSprinting()Z"))
    private boolean dontUnsprint(ClientPlayerEntity instance) {
        return shouldStopSprinting() && ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_21_4);
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendSneakingPacket()V"))
    private boolean sendSneakingAfterSprinting(ClientPlayerEntity instance) {
        return ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_21_2);
    }

    @Inject(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendSprintingPacket()V", shift = At.Shift.AFTER))
    private void sendSneakingAfterSprinting(CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21)) {
            this.sendSneakingPacket();
        }
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendSprintingPacket()V"))
    private boolean removeSprintingPacket(ClientPlayerEntity instance) {
        return ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_19_3);
    }

    @Redirect(method = "canSprint", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasVehicle()Z"))
    private boolean dontAllowSprintingAsPassenger(ClientPlayerEntity instance) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_19_1) && instance.hasVehicle();
    }

    @Redirect(method = "tickMovement()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;hasForwardMovement()Z", ordinal = 0))
    private boolean disableSprintSneak(Input input) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_1)) {
            return input.movementForward >= 0.8F;
        } else {
            return input.hasForwardMovement();
        }
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAttributeValue(Lnet/minecraft/registry/entry/RegistryEntry;)D"))
    private void removeSneakingConditions(CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
            this.inSneakingPose = this.isSneaking() && !this.isSleeping();
        }
    }

}
