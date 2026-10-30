package com.viaversion.viafabricplus.injection.mixin.features.movement.vehicle;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.Packet;

import com.mojang.authlib.GameProfile;
import com.viaversion.viafabricplus.injection.access.base.IClientConnection;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import net.raphimc.vialegacy.protocol.release.r1_5_2tor1_6_1.Protocolr1_5_2Tor1_6_1;
import net.raphimc.vialegacy.protocol.release.r1_5_2tor1_6_1.packet.ServerboundPackets1_5_2;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientPlayerEntity.class, priority = 2000)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

    @Shadow
    @Final
    public ClientPlayNetworkHandler networkHandler;

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasVehicle()Z", ordinal = 0))
    private boolean removeVehicleRequirement(ClientPlayerEntity instance) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_20) && instance.hasVehicle();
    }

    @Redirect(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasVehicle()Z"))
    private boolean removeVehicleCheck(ClientPlayerEntity instance) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_19_3) && instance.hasVehicle();
    }

    @Inject(method = "startRiding", at = @At("RETURN"))
    private void setRotationsWhenInBoat(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && entity instanceof BoatEntity && ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_18)) {
            this.prevYaw = entity.getYaw();
            this.setYaw(entity.getYaw());
            this.setHeadYaw(entity.getYaw());
        }
    }

    @Redirect(method = "tick", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasVehicle()Z")), at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 0))
    private void modifyPositionPacket(ClientPlayNetworkHandler instance, Packet<?> packet) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.r1_5_2)) {
            final PacketWrapper movePlayerPosRot = PacketWrapper.create(ServerboundPackets1_5_2.MOVE_PLAYER_POS_ROT, ((IClientConnection) this.networkHandler.getConnection()).viaFabricPlus$getUserConnection());
            movePlayerPosRot.write(Types.DOUBLE, this.getVelocity().x);
            movePlayerPosRot.write(Types.DOUBLE, -999.0D);
            movePlayerPosRot.write(Types.DOUBLE, -999.0D);
            movePlayerPosRot.write(Types.DOUBLE, this.getVelocity().z);
            movePlayerPosRot.write(Types.FLOAT, this.getYaw());
            movePlayerPosRot.write(Types.FLOAT, this.getPitch());
            movePlayerPosRot.write(Types.BOOLEAN, this.isOnGround());
            movePlayerPosRot.scheduleSendToServer(Protocolr1_5_2Tor1_6_1.class);
            return;
        }
        instance.sendPacket(packet);
    }

}
