package ru.he3hauka.toolvisuals.mixin.voicechat;

import de.maxhenkel.voicechat.voice.client.RenderEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.voice.VoiceChat;

@Mixin(value=RenderEvents.class,remap=false)
public abstract class VoiceHudMixin {
    @Inject(method="onRenderHUD",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$hideDisabledVoiceHud(CallbackInfo callback){if(!VoiceChat.policy().enabled())callback.cancel();}
}
