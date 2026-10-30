package com.viaversion.viafabricplus.injection.mixin.features.movement;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;

import com.mojang.authlib.GameProfile;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ClientPlayerEntity.class, priority = 2000)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

    @Shadow
    public Input input;

    @Shadow
    @Final
    protected MinecraftClient client;

    @Shadow
    private int ticksSinceLastPositionPacketSent;

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;ticksSinceLastPositionPacketSent:I", ordinal = 0))
    private int moveLastPosPacketIncrement(ClientPlayerEntity instance) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            return this.ticksSinceLastPositionPacketSent - 1;
        } else {
            return this.ticksSinceLastPositionPacketSent;
        }
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;ticksSinceLastPositionPacketSent:I", ordinal = 2))
    private int moveLastPosPacketIncrement2(ClientPlayerEntity instance) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            return this.ticksSinceLastPositionPacketSent++;
        } else {
            return this.ticksSinceLastPositionPacketSent;
        }
    }

}
