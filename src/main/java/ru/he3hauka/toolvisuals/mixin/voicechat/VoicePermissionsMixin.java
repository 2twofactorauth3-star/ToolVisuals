package ru.he3hauka.toolvisuals.mixin.voicechat;

import de.maxhenkel.voicechat.voice.client.ClientManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.voice.VoiceChat;

@Mixin(value=ClientManager.class,remap=false)
public abstract class VoicePermissionsMixin {
    @Inject(method="checkMicrophonePermissions",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$skipDisabledMicrophonePrompt(CallbackInfo callback){if(!VoiceChat.policy().enabled())callback.cancel();}
}
