package ru.he3hauka.toolvisuals.mixin.voicechat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.voice.VoiceControls;

@Mixin(Minecraft.class)
public abstract class VoiceScreenMixin {
    @Inject(method="setScreen(Lnet/minecraft/client/gui/screens/Screen;)V",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$skipDisabledScreen(Screen screen,CallbackInfo callback){if(VoiceControls.blockedScreen(screen))callback.cancel();}
}
