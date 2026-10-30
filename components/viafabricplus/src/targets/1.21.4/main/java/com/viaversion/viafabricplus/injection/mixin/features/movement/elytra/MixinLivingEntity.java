package com.viaversion.viafabricplus.injection.mixin.features.movement.elytra;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = "calcGlidingVelocity", at = @At(value = "INVOKE", target = "Ljava/lang/Math;cos(D)D", remap = false))
    private double fixCosTable(double a) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_18)) {
            return MathHelper.cos((float) a);
        } else {
            return Math.cos(a);
        }
    }

    @Redirect(method = "canGlide", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z"))
    private boolean allowElytraWhenLevitating(LivingEntity instance, RegistryEntry<StatusEffect> effect) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_15_2) && instance.hasStatusEffect(effect);
    }

    @Redirect(method = "canGlide", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasVehicle()Z"))
    private boolean allowElytraInVehicle(LivingEntity instance) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_14_4) && instance.hasVehicle();
    }

}
