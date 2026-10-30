package com.viaversion.viafabricplus.injection.mixin.features.movement.water;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.world.World;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkeletonHorseEntity.class)
public abstract class MixinSkeletonHorseEntity extends AbstractHorseEntity {

    protected MixinSkeletonHorseEntity(EntityType<? extends AbstractHorseEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "getBaseWaterMovementSpeedMultiplier", at = @At("HEAD"), cancellable = true)
    private void modifyBaseWaterMovementSpeedMultiplier(CallbackInfoReturnable<Float> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            cir.setReturnValue(super.getBaseWaterMovementSpeedMultiplier());
        }
    }

}
