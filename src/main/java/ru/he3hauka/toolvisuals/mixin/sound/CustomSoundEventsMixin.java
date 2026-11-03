package ru.he3hauka.toolvisuals.mixin.sound;

import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.sound.library.CustomSounds;

@Mixin(SoundManager.class)
public abstract class CustomSoundEventsMixin {
    @Inject(method="getSoundEvent",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$customSound(Identifier id,CallbackInfoReturnable<WeighedSoundEvents> callback){
        var sound=CustomSounds.resolve(id);if(sound!=null)callback.setReturnValue(sound);
    }
}
