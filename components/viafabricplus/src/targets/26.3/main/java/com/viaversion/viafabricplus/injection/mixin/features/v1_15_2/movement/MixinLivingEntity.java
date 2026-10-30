package com.viaversion.viafabricplus.injection.mixin.features.v1_15_2.movement;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.core.Holder;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Redirect(method = "canGlide", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean allowElytraWhenLevitating(LivingEntity instance, Holder<MobEffect> effect) {
        return ViaFabricPlus.api().targetVersion().newerThan(ProtocolVersion.v1_15_2) && instance.hasEffect(effect);
    }

    @WrapOperation(method = "drop", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;swing(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;Z)Z"))
    private boolean dontSwingHand(LivingEntity instance, InteractionHand hand, SwingAnimation animation, boolean sendToSwingingEntity, Operation<Boolean> original) {
        if (ViaFabricPlus.api().targetVersion().newerThan(ProtocolVersion.v1_15_2)) {
            return original.call(instance, hand, animation, sendToSwingingEntity);
        } else {
            return false;
        }
    }

    @Redirect(method = "isInShallowFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getFluidHeight(Lnet/minecraft/tags/TagKey;)D"))
    private double dontApplyLavaMovement(LivingEntity instance, TagKey<Fluid> tagKey) {
        if (ViaFabricPlus.api().targetVersion().olderThanOrEqualTo(ProtocolVersion.v1_15_2) && tagKey == FluidTags.LAVA) {
            return Double.MAX_VALUE;
        } else {
            return instance.getFluidHeight(tagKey);
        }
    }

}
