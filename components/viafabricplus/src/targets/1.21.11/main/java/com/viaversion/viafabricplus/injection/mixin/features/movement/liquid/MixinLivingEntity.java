package com.viaversion.viafabricplus.injection.mixin.features.movement.liquid;

import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
   public MixinLivingEntity(EntityType<?> type, Level world) {
      super(type, world);
   }

   @Shadow
   protected abstract float getWaterSlowDown();

   @Shadow
   public abstract boolean isJumping();

   @Redirect(
      method = "travelInLava",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getFluidHeight(Lnet/minecraft/tags/TagKey;)D")
   )
   private double dontApplyLavaMovement(LivingEntity instance, TagKey<Fluid> tagKey) {
      return ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_15_2) ? Double.MAX_VALUE : instance.getFluidHeight(tagKey);
   }

   @Redirect(
      method = "travelInWater",
      slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/effect/MobEffects;DOLPHINS_GRACE:Lnet/minecraft/core/Holder;")),
      at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;horizontalCollision:Z", ordinal = 0)
   )
   private boolean disableClimbing(LivingEntity instance) {
      return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_13_2) && instance.horizontalCollision;
   }

   @ModifyVariable(method = "getFluidFallingAdjustedMovement", ordinal = 0, at = @At("HEAD"), argsOnly = true)
   private boolean modifyMovingDown(boolean movingDown) {
      return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_13_2) && movingDown;
   }

   @Redirect(method = "travelInWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSprinting()Z", ordinal = 0))
   private boolean modifySwimSprintSpeed(LivingEntity instance) {
      return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_12_2) && instance.isSprinting();
   }

   @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getFluidHeight(Lnet/minecraft/tags/TagKey;)D"))
   private double redirectFluidHeight(LivingEntity var1, TagKey<Fluid> var2) {
      if (!ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
         ProtocolTranslator.getTargetVersion();
         Object var10001 = null;
         boolean var10000 = false;
      } else if (var2 == FluidTags.WATER && this.isInWater()) {
         return 1.0;
      }

      return var1.getFluidHeight(var2);
   }

   @Inject(method = "getFluidFallingAdjustedMovement", at = @At("HEAD"), cancellable = true)
   private void modifySwimSprintFallSpeed(double gravity, boolean movingDown, Vec3 velocity, CallbackInfoReturnable<Vec3> ci) {
      if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2) && !this.isNoGravity()) {
         ci.setReturnValue(new Vec3(velocity.x, velocity.y - 0.02, velocity.z));
      }
   }

   @ModifyConstant(method = "travelInWater", constant = @Constant(floatValue = 0.9F))
   private float modifySwimFriction(float constant) {
      return ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2) ? this.getWaterSlowDown() : constant;
   }
}
