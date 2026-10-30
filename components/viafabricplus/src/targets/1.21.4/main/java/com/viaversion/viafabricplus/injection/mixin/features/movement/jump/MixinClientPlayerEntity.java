package com.viaversion.viafabricplus.injection.mixin.features.movement.jump;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.authlib.GameProfile;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @WrapWithCondition(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;jump()V"))
    private boolean dontJumpBeforeFlying(ClientPlayerEntity instance) {
        return ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_20_5);
    }

    @Redirect(method = "autoJump", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;inverseSqrt(F)F"))
    private float useFastInverseSqrt(float x) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_19_3)) {
            x = Float.intBitsToFloat(1597463007 - (Float.floatToIntBits(x) >> 1));
            return x * (1.5F - (0.5F * x) * x * x);
        } else {
            return MathHelper.inverseSqrt(x);
        }
    }

}
