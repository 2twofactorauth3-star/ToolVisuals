package ru.he3hauka.toolvisuals.mixin.voicechat;

import de.maxhenkel.voicechat.debug.DebugOverlay;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.voice.VoiceChat;

@Mixin(value=DebugOverlay.class,remap=false)
public abstract class VoiceDebugMixin {
    @Inject(method="toggle",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$skipToggle(CallbackInfo callback){if(!VoiceChat.policy().enabled())callback.cancel();}
    @Inject(method="render",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$skipRender(GuiGraphicsExtractor graphics,float partial,CallbackInfo callback){if(!VoiceChat.policy().enabled())callback.cancel();}
}
