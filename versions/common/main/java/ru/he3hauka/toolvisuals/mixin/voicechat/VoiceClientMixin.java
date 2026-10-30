package ru.he3hauka.toolvisuals.mixin.voicechat;

import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.voice.VoiceChat;

@Mixin(value=ClientVoicechat.class,remap=false)
public abstract class VoiceClientMixin {
    @Inject(method="processSoundPacket",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$skipDisabledPlayback(SoundPacket packet,CallbackInfo callback){if(!VoiceChat.policy().receive())callback.cancel();}
    @Inject(method="setRecording",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$skipDisabledRecording(boolean recording,CallbackInfoReturnable<Boolean> callback){if(recording&&!VoiceChat.policy().enabled())callback.setReturnValue(false);}
    @Inject(method="startMicThread",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$preventDisabledDeviceStartup(CallbackInfo callback){if(!VoiceChat.policy().enabled())callback.cancel();}
}
