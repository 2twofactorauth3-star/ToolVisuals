package com.viaversion.viafabricplus.injection.mixin.features.v26_2.movement;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityFluidInteraction.class)
public abstract class MixinEntityFluidInteraction {

    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;getHeightForCamera(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
    private float useFluidHeightForEyes(FluidState instance, BlockGetter level, BlockPos pos, Operation<Float> original) {
        if (ViaFabricPlus.api().targetVersion().olderThanOrEqualTo(ProtocolVersion.v26_2)) {
            return instance.getHeight(level, pos);
        } else {
            return original.call(instance, level, pos);
        }
    }

}
