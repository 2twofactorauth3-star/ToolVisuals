package com.viaversion.viafabricplus.visuals.injection.mixin.player_rotations;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    @Shadow
    public float bodyYaw;

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = "turnHead", at = @At(value = "INVOKE", target = "Ljava/lang/Math;abs(F)F"))
    private float changeBodyRotationInterpolation(float g) {
        if (VisualSettings.INSTANCE.changeBodyRotationInterpolation.isEnabled()) {
            g = MathHelper.clamp(g, -75.0F, 75.0F);
            this.bodyYaw = this.getYaw() - g;
            if (Math.abs(g) > 50.0F) {
                this.bodyYaw += g * 0.2F;
            }
            return Float.MIN_VALUE;
        } else {
            return Math.abs(g);
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;abs(F)F"))
    private float alwaysRotateWhenWalkingBackwards(float value) {
        if (VisualSettings.INSTANCE.sidewaysBackwardsRunning.isEnabled()) {
            return 0F;
        } else {
            return MathHelper.abs(value);
        }
    }

}
