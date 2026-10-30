package ru.he3hauka.toolvisuals.mixin.voicechat;

import de.maxhenkel.voicechat.voice.client.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value=ClientVoicechat.class,remap=false)
public interface VoiceMicInvoker {
    @Invoker("startMicThread") void toolvisuals$startMic(ClientVoicechatConnection connection);
}
