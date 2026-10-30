package com.viaversion.viafabricplus.injection.mixin.features.movement.slowdown;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;

import com.mojang.authlib.GameProfile;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPlayerEntity.class, priority = 2000)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

    @Shadow
    public Input input;

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Redirect(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;shouldSlowDown()Z"))
    private boolean removeSlowdownCondition(ClientPlayerEntity instance) {
        return instance.shouldSlowDown() && ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_21_4);
    }

    @Redirect(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSubmergedInWater()Z"))
    private boolean removeSlowdownCondition2(ClientPlayerEntity instance) {
        return instance.isSubmergedInWater() && ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_21_4);
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;shouldSlowDown()Z"))
    private boolean changeSneakSlowdownCondition(ClientPlayerEntity instance) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
            return instance.input.playerInput.sneak();
        } else if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_4)) {
            return !MinecraftClient.getInstance().player.isSpectator() && (instance.input.playerInput.sneak() || instance.shouldSlowDown());
        } else {
            return instance.shouldSlowDown();
        }
    }

    @Inject(method = "tickMovement()V",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isCamera()Z")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/PlayerInput;sneak()Z", ordinal = 0))
    private void undoSneakSlowdownForFly(CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().betweenInclusive(ProtocolVersion.v1_9, ProtocolVersion.v1_14_4)) {
            if (this.input.playerInput.sneak()) {
                this.input.movementSideways = (float) ((double) this.input.movementSideways / 0.3D);
                this.input.movementForward = (float) ((double) this.input.movementForward / 0.3D);
            }
        }
    }

}
