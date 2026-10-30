package ru.he3hauka.toolvisuals.mixin.voicechat;

import de.maxhenkel.voicechat.gui.onboarding.OnboardingManager;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.voice.VoiceChat;

@Mixin(value=OnboardingManager.class,remap=false)
public abstract class VoiceOnboardingMixin {
    @Inject(method="onConnecting",at=@At("HEAD"),cancellable=true)
    private static void toolvisuals$skipDisabledOnboarding(CallbackInfo callback){if(!VoiceChat.policy().enabled())callback.cancel();}
    @Inject(method="startOnboarding",at=@At("HEAD"),cancellable=true)
    private static void toolvisuals$skipDisabledSetup(Screen parent,CallbackInfo callback){if(!VoiceChat.policy().enabled())callback.cancel();}
}
