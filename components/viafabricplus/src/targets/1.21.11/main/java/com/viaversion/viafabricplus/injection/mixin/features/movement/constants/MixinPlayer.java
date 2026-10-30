package com.viaversion.viafabricplus.injection.mixin.features.movement.constants;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayer extends LivingEntity {

    protected MixinPlayer(final EntityType<? extends LivingEntity> entityType, final Level world) {
        super(entityType, world);
    }

    @Inject(method = "canFallAtLeast", at = @At("HEAD"), cancellable = true)
    private void changeOffsetsForSneakingCollisionDetection(double offsetX, double offsetZ, double d, CallbackInfoReturnable<Boolean> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21_4)) {
            final double constant;
            if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_3)) {
                constant = 0.0;
            } else {
                constant = 1.0E-5F;
            }
            final AABB box = getBoundingBox();
            cir.setReturnValue(level().noCollision(this, new AABB(box.minX + offsetX, box.minY - d - constant, box.minZ + offsetZ, box.maxX + offsetX, box.minY, box.maxZ + offsetZ)));
        }
    }

    @Redirect(method = "maybeBackOffFromEdge", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;maxUpStep()F"))
    private float modifyStepHeight(Player instance) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_10)) {
            return 1.0F;
        } else {
            return instance.maxUpStep();
        }
    }

}
