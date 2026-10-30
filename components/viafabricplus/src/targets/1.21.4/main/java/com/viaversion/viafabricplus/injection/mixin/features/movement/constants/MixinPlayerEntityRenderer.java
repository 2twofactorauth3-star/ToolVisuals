package com.viaversion.viafabricplus.injection.mixin.features.movement.constants;

import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public abstract class MixinPlayerEntityRenderer {

    @Inject(method = "getPositionOffset(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)Lnet/minecraft/util/math/Vec3d;", at = @At("RETURN"), cancellable = true)
    private void modifySleepingOffset(PlayerEntityRenderState playerEntityRenderState, CallbackInfoReturnable<Vec3d> cir) {
        if (playerEntityRenderState.pose == EntityPose.SLEEPING) {
            final Direction sleepingDir = playerEntityRenderState.sleepingDirection;
            if (sleepingDir != null) {
                if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
                    cir.setReturnValue(cir.getReturnValue().subtract(sleepingDir.getOffsetX() * 0.4, 0, sleepingDir.getOffsetZ() * 0.4));
                }
                if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.b1_5tob1_5_2)) {
                    cir.setReturnValue(cir.getReturnValue().subtract(sleepingDir.getOffsetX() * 0.1, 0, sleepingDir.getOffsetZ() * 0.1));
                }
                if (ProtocolTranslator.getTargetVersion().betweenInclusive(LegacyProtocolVersion.b1_6tob1_6_6, ProtocolVersion.v1_7_6)) {
                    cir.setReturnValue(cir.getReturnValue().subtract(0, 0.3F, 0));
                }
            }
        }
    }

    @Redirect(method = "getPositionOffset(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)Lnet/minecraft/util/math/Vec3d;",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;isInSneakingPose:Z"))
    private boolean disableSneakPositionOffset(PlayerEntityRenderState instance) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_11_1) && instance.isInSneakingPose;
    }

}
