package ru.he3hauka.toolvisuals.mixin.sound;

import com.mojang.blaze3d.audio.SoundBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.sound.library.CustomSounds;

@Mixin(SoundBufferLibrary.class)
public abstract class CustomSoundBuffersMixin {
    @Shadow @Final private Map<Identifier,CompletableFuture<SoundBuffer>> cache;
    @Inject(method="getCompleteBuffer",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$customBuffer(Identifier id,CallbackInfoReturnable<CompletableFuture<SoundBuffer>> callback){
        if(CustomSounds.owns(id))callback.setReturnValue(CustomSounds.buffer(id,cache));
    }
}
