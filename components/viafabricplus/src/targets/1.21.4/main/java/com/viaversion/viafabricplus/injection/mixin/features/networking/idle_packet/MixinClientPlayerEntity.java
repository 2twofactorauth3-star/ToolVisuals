package com.viaversion.viafabricplus.injection.mixin.features.networking.idle_packet;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;

import com.mojang.authlib.GameProfile;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ClientPlayerEntity.class, priority = 2000)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

    @Shadow
    private boolean lastOnGround;

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;lastOnGround:Z", ordinal = 0))
    private boolean sendIdlePacket(ClientPlayerEntity instance) {
        if (ProtocolTranslator.getTargetVersion().betweenInclusive(LegacyProtocolVersion.r1_4_2, ProtocolVersion.v1_8) || ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.r1_2_4tor1_2_5)) {
            return !isOnGround();
        } else {
            return this.lastOnGround;
        }
    }

}
