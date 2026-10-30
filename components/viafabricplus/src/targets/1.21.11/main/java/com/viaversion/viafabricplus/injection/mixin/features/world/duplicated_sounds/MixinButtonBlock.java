package com.viaversion.viafabricplus.injection.mixin.features.world.duplicated_sounds;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ButtonBlock;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.viaversion.viafabricplus.settings.impl.DebugSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ButtonBlock.class)
public abstract class MixinButtonBlock {

    @WrapWithCondition(
        method = "playSound",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;)V"))
    private boolean disableClickSounds(LevelAccessor instance, Entity source, BlockPos pos, SoundEvent sound, SoundSource category) {
        return !DebugSettings.INSTANCE.serversidePlaceSounds.isEnabled();
    }

}
