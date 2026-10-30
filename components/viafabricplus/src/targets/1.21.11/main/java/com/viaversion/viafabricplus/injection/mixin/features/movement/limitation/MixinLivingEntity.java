package com.viaversion.viafabricplus.injection.mixin.features.movement.limitation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    public MixinLivingEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow
    public abstract boolean hasEffect(Holder<MobEffect> effect);

    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Ljava/lang/Object;equals(Ljava/lang/Object;)Z"))
    private boolean useEuclideanDistanceCalculation(Object instance, Object o) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21_4) && instance instanceof EntityType<?>) {
            return false;
        } else {
            return instance.equals(o);
        }
    }

    @Redirect(method = "travelInAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;hasChunkAt(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean modifyLoadedCheck(Level instance, BlockPos blockPos) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
            return this.level().hasChunkAt(blockPos) && instance.getChunkSource().hasChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        } else {
            return this.level().hasChunkAt(blockPos);
        }
    }

    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;resetFallDistance()V"))
    private void dontResetLevitationFallDistance(LivingEntity instance) {

        if (this.hasEffect(MobEffects.SLOW_FALLING) || ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_12_2)) {
            instance.resetFallDistance();
        }
    }

}
