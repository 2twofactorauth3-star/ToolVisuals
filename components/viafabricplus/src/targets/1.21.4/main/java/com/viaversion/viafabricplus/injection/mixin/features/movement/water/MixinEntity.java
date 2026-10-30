package com.viaversion.viafabricplus.injection.mixin.features.movement.water;

import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

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
    private World world;

    @Shadow
    public abstract Box getBoundingBox();

    @Shadow
    public abstract Vec3d getVelocity();

    @Shadow
    public abstract void setVelocity(Vec3d velocity);

    @Redirect(method = "updateSubmergedInWaterState", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getEyeY()D"))
    private double addMagicOffset(Entity instance) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_3)) {
            return instance.getEyeY() - 0.11111111F;
        } else {
            return instance.getEyeY();
        }
    }

    @Inject(method = "setSwimming", at = @At("HEAD"), cancellable = true)
    private void cancelSwimming(boolean swimming, CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2) && swimming) {
            ci.cancel();
        }
    }

    @Inject(method = "updateMovementInFluid", at = @At("HEAD"), cancellable = true)
    private void modifyFluidMovementBoundingBox(TagKey<Fluid> fluidTag, double d, CallbackInfoReturnable<Boolean> cir) {
        if (ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_12_2)) {
            return;
        }

        Box box = this.getBoundingBox().expand(0, -0.4, 0).contract(0.001);
        int minX = MathHelper.floor(box.minX);
        int maxX = MathHelper.ceil(box.maxX);
        int minY = MathHelper.floor(box.minY);
        int maxY = MathHelper.ceil(box.maxY);
        int minZ = MathHelper.floor(box.minZ);
        int maxZ = MathHelper.ceil(box.maxZ);

        if (!this.world.isRegionLoaded(minX, minY, minZ, maxX, maxY, maxZ)) {
            cir.setReturnValue(false);
            return;
        }

        double waterHeight = 0;
        boolean foundFluid = false;
        Vec3d pushVec = Vec3d.ZERO;

        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int x = minX; x < maxX; x++) {
            for (int y = minY - 1; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    mutable.set(x, y, z);
                    FluidState state = this.world.getFluidState(mutable);
                    if (state.isIn(fluidTag)) {
                        double height = y + state.getHeight(this.world, mutable);
                        if (height >= box.minY - 0.4)
                            waterHeight = Math.max(height - box.minY + 0.4, waterHeight);
                        if (y >= minY && maxY >= height) {
                            foundFluid = true;
                            pushVec = pushVec.add(state.getVelocity(this.world, mutable));
                        }
                    }
                }
            }
        }

        if (pushVec.length() > 0) {
            pushVec = pushVec.normalize().multiply(0.014);
            this.setVelocity(this.getVelocity().add(pushVec));
        }

        this.fluidHeight.put(fluidTag, waterHeight);
        cir.setReturnValue(foundFluid);
    }

}
