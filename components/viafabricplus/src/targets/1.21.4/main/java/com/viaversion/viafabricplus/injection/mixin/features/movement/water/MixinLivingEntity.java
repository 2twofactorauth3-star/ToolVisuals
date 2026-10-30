package com.viaversion.viafabricplus.injection.mixin.features.movement.water;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    protected abstract float getBaseWaterMovementSpeedMultiplier();

    @Redirect(method = "travelInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getFluidHeight(Lnet/minecraft/registry/tag/TagKey;)D"))
    private double dontApplyLavaMovement(LivingEntity instance, TagKey<Fluid> tagKey) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_15_2)) {
            return Double.MAX_VALUE;
        } else {
            return instance.getFluidHeight(tagKey);
        }
    }

    @Redirect(method = "travelInFluid",
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/entity/effect/StatusEffects;DOLPHINS_GRACE:Lnet/minecraft/registry/entry/RegistryEntry;")),
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;horizontalCollision:Z", ordinal = 0))
    private boolean disableClimbing(LivingEntity instance) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_13_2) && instance.horizontalCollision;
    }

    @ModifyVariable(method = "applyFluidMovingSpeed", ordinal = 0, at = @At("HEAD"), argsOnly = true)
    private boolean modifyMovingDown(boolean movingDown) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_13_2) && movingDown;
    }

    @Redirect(method = "travelInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSprinting()Z", ordinal = 0))
    private boolean modifySwimSprintSpeed(LivingEntity instance) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_12_2) && instance.isSprinting();
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getFluidHeight(Lnet/minecraft/registry/tag/TagKey;)D"))
    private double redirectFluidHeight(LivingEntity instance, TagKey<Fluid> tagKey) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2) && tagKey == FluidTags.WATER) {
            if (instance.getFluidHeight(tagKey) > 0) return 1;
        }
        return instance.getFluidHeight(tagKey);
    }

    @Inject(method = "applyFluidMovingSpeed", at = @At("HEAD"), cancellable = true)
    private void modifySwimSprintFallSpeed(double gravity, boolean movingDown, Vec3d velocity, CallbackInfoReturnable<Vec3d> ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2) && !this.hasNoGravity()) {
            ci.setReturnValue(new Vec3d(velocity.x, velocity.y - 0.02, velocity.z));
        }
    }

    @ModifyConstant(method = "travelInFluid", constant = @Constant(floatValue = 0.9F))
    private float modifySwimFriction(float constant) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            return this.getBaseWaterMovementSpeedMultiplier();
        } else {
            return constant;
        }
    }

}
