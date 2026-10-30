package com.viaversion.viafabricplus.injection.mixin.features.movement.constants;

import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.EntityAttachments;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity {

    @Unique
    private static final EntityDimensions viaFabricPlus$sneaking_dimensions_v1_13_2 = EntityDimensions.changing(0.6F, 1.65F).withEyeHeight(1.54F).
            withAttachments(EntityAttachments.builder().add(EntityAttachmentType.VEHICLE, PlayerEntity.VEHICLE_ATTACHMENT_POS));

    @Unique
    private static final EntityDimensions viaFabricPlus$sneaking_dimensions_v1_8 = EntityDimensions.changing(0.6F, 1.8F).withEyeHeight(1.54F).
            withAttachments(EntityAttachments.builder().add(EntityAttachmentType.VEHICLE, PlayerEntity.VEHICLE_ATTACHMENT_POS));

    @ModifyConstant(method = "isSpaceAroundPlayerEmpty", constant = @Constant(doubleValue = 9.999999747378752E-6  ))
    private double removeOffsetWhenCheckingSneakingCollision(double constant) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_3)) {
            return 0;
        } else {
            return constant;
        }
    }

    @Inject(method = "getBaseDimensions", at = @At("HEAD"), cancellable = true)
    private void modifyDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (pose == EntityPose.CROUCHING) {
            if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
                cir.setReturnValue(viaFabricPlus$sneaking_dimensions_v1_8);
            } else if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
                cir.setReturnValue(viaFabricPlus$sneaking_dimensions_v1_13_2);
            }
        }
    }

    @Redirect(method = "adjustMovementForSneaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getStepHeight()F"))
    private float modifyStepHeight(PlayerEntity instance) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_10)) {
            return 1.0F;
        } else {
            return instance.getStepHeight();
        }
    }

}
