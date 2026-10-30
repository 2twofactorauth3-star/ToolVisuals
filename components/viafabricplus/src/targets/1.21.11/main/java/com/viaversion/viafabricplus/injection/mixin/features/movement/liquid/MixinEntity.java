package com.viaversion.viafabricplus.injection.mixin.features.movement.liquid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {
   @Shadow
   protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;
   @Shadow
   private Level level;

   @Shadow
   public abstract AABB getBoundingBox();

   @Shadow
   public abstract Vec3 getDeltaMovement();

   @Shadow
   public abstract void setDeltaMovement(Vec3 var1);

   @Redirect(method = "updateFluidOnEyes", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getEyeY()D"))
   private double subtractMagicOffset(Entity instance) {
      return ProtocolTranslator.getTargetVersion().betweenInclusive(ProtocolVersion.v1_16, ProtocolVersion.v1_20_3)
         ? instance.getEyeY() - 0.11111111F
         : instance.getEyeY();
   }

   @Redirect(
      method = "updateFluidOnEyes",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/level/material/FluidState;getHeight(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"
      )
   )
   private float addMagicOffset(FluidState instance, BlockGetter blockGetter, BlockPos blockPos) {
      return ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_15_2)
         ? instance.getHeight(blockGetter, blockPos) + 0.11111111F
         : instance.getHeight(blockGetter, blockPos);
   }

   @Inject(method = "isInLava", at = @At("RETURN"), cancellable = true)
   private void replaceLavaCheck1_13_2(CallbackInfoReturnable<Boolean> cir) {
      if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
         AABB aabb = this.getBoundingBox().deflate(0.1F, 0.4F, 0.1F);
         cir.setReturnValue(this.level.getBlockStatesIfLoaded(aabb).anyMatch(key -> key.getFluidState().is(FluidTags.LAVA)));
      }
   }

   @Inject(method = "setSwimming", at = @At("HEAD"), cancellable = true)
   private void cancelSwimming(boolean swimming, CallbackInfo ci) {
      if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2) && swimming) {
         ci.cancel();
      }
   }

   @Inject(method = "updateFluidHeightAndDoFluidPushing", at = @At("HEAD"), cancellable = true)
   private void modifyFluidMovementBoundingBox(TagKey<Fluid> var1, double var2, CallbackInfoReturnable<Boolean> var4) {
      if (ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_12_2)) {
         ProtocolTranslator.getTargetVersion();
         Object var10001 = null;
         boolean var10000 = false;
      } else {
         AABB var5 = this.getBoundingBox().inflate(0.0, -0.4, 0.0).deflate(0.001);
         int var6 = Mth.floor(var5.minX);
         int var7 = Mth.ceil(var5.maxX);
         int var8 = Mth.floor(var5.minY);
         int var9 = Mth.ceil(var5.maxY);
         int var10 = Mth.floor(var5.minZ);
         int var11 = Mth.ceil(var5.maxZ);
         if (!this.level.hasChunksAt(var6, var8, var10, var7, var9, var11)) {
            var4.setReturnValue(false);
         } else {
            double var12 = 0.0;
            boolean var14 = false;
            Vec3 var15 = Vec3.ZERO;
            MutableBlockPos var16 = new MutableBlockPos();

            for (int var17 = var6; var17 < var7; var17++) {
               for (int var18 = var8 - 1; var18 < var9; var18++) {
                  for (int var19 = var10; var19 < var11; var19++) {
                     var16.set(var17, var18, var19);
                     FluidState var20 = this.level.getFluidState(var16);
                     if (var20.is(var1)) {
                        double var21 = var18 + var20.getHeight(this.level, var16);
                        if (var21 >= var5.minY - 0.4) {
                           var12 = Math.max(var21 - var5.minY + 0.4, var12);
                        }

                        if (var18 >= var8 && var9 >= var21) {
                           var14 = true;
                           var15 = var15.add(var20.getFlow(this.level, var16));
                        }
                     }
                  }
               }
            }

            if (var15.length() > 0.0) {
               var15 = var15.normalize().scale(0.014);
               this.setDeltaMovement(this.getDeltaMovement().add(var15));
            }

            this.fluidHeight.put(var1, var12);
            var4.setReturnValue(var14);
         }
      }
   }
}
