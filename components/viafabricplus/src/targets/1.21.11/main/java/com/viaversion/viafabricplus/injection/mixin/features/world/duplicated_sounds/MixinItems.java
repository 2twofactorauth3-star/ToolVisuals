package com.viaversion.viafabricplus.injection.mixin.features.world.duplicated_sounds;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.level.Level;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.viaversion.viafabricplus.settings.impl.DebugSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({FlintAndSteelItem.class, HoeItem.class})
public abstract class MixinItems {

    @WrapWithCondition(method = "useOn", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private boolean disableItemPlaceSounds(Level instance, Entity source, BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch) {
        return !DebugSettings.INSTANCE.serversidePlaceSounds.isEnabled();
    }

}
