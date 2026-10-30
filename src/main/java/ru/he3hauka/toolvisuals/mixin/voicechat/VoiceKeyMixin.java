package ru.he3hauka.toolvisuals.mixin.voicechat;

import de.maxhenkel.voicechat.voice.client.KeyEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.voice.VoiceControls;

@Mixin(value=KeyEvents.class,remap=false)
public abstract class VoiceKeyMixin {
    @Inject(method="handleKeybinds",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$disableKeybinds(CallbackInfo callback){if(!VoiceControls.inputAllowed())callback.cancel();}
}
