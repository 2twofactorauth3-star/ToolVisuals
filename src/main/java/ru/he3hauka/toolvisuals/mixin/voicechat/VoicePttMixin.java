package ru.he3hauka.toolvisuals.mixin.voicechat;

import de.maxhenkel.voicechat.voice.client.PTTKeyHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import ru.he3hauka.toolvisuals.client.voice.VoiceChat;

@Mixin(value=PTTKeyHandler.class,remap=false)
public abstract class VoicePttMixin {
    @Shadow private boolean pttKeyDown;
    @Shadow private boolean whisperKeyDown;
    @Inject(method="onKeyboardEvent",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$skipKeyboard(CallbackInfo callback){if(!VoiceChat.policy().enabled()){pttKeyDown=whisperKeyDown=false;callback.cancel();}}
    @Inject(method="onMouseEvent",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$skipMouse(CallbackInfo callback){if(!VoiceChat.policy().enabled()){pttKeyDown=whisperKeyDown=false;callback.cancel();}}
    @Inject(method={"isPTTDown","isWhisperDown","isAnyDown"},at=@At("HEAD"),cancellable=true)
    private void toolvisuals$releasedWhileDisabled(CallbackInfoReturnable<Boolean> callback){if(!VoiceChat.policy().enabled()){pttKeyDown=whisperKeyDown=false;callback.setReturnValue(false);}}
}
