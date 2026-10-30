package ru.he3hauka.toolvisuals.mixin.voicechat;

import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.voice.VoiceChat;

@Mixin(value=ClientPlayerStateManager.class,remap=false)
public abstract class VoiceStateMixin {
    @Inject(method="isDisabled",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$disabled(CallbackInfoReturnable<Boolean> callback){if(!VoiceChat.policy().enabled())callback.setReturnValue(true);}
    @Inject(method="isMuted",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$muted(CallbackInfoReturnable<Boolean> callback){if(!VoiceChat.policy().transmit())callback.setReturnValue(true);}
    @Inject(method="setDisabled",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$nativeToggle(boolean disabled,CallbackInfo callback){if(!disabled&&!VoiceChat.policy().enabled())callback.cancel();}
    @Inject(method="setMuted",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$blockDisabledUnmute(boolean muted,CallbackInfo callback){if(!muted&&!VoiceChat.policy().enabled())callback.cancel();}
    @Inject(method="setMuted",at=@At("RETURN"))
    private void toolvisuals$nativeMicrophone(boolean muted,CallbackInfo callback){var config=ToolVisualsClient.CONFIG;if(config.active(VisualModule.VOICE_CHAT)&&config.flag(VisualSetting.VOICE_MICROPHONE)==muted)config.setValue(VisualSetting.VOICE_MICROPHONE,muted?0:1);}
}
