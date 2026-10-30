package com.viaversion.viafabricplus.injection.mixin.features.entity.pose;

import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity {

    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "updatePose", at = @At("HEAD"), cancellable = true)
    private void onUpdatePose(CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
            final EntityPose pose;
            if (this.isGliding()) {
                pose = EntityPose.GLIDING;
            } else if (this.isSleeping()) {
                pose = EntityPose.SLEEPING;
            } else if (this.isSwimming()) {
                pose = EntityPose.SWIMMING;
            } else if (this.isUsingRiptide()) {
                pose = EntityPose.SPIN_ATTACK;
            } else if (this.isSneaking()) {
                pose = EntityPose.CROUCHING;
            } else {
                pose = EntityPose.STANDING;
            }
            this.setPose(pose);
            ci.cancel();
        }
    }

}
