package ru.he3hauka.toolvisuals.mixin.sound;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.sound.SoundCustomizer;

@Mixin(SoundEngine.class)
public abstract class SoundCustomizerMixin {
    @Inject(method="play",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$replaceHitSound(SoundInstance sound,org.spongepowered.asm.mixin.injection.callback.CallbackInfo cir) {
        if(SoundCustomizer.suppressVanilla(sound))cir.cancel();
    }
}
