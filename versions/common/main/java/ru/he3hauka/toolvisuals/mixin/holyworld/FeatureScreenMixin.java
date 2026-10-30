package ru.he3hauka.toolvisuals.mixin.holyworld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.connection.policy.FeatureScreens;

@Mixin(Minecraft.class)
public abstract class FeatureScreenMixin {
    @Inject(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$restrictedScreen(Screen screen, CallbackInfo callback) {
        if (FeatureScreens.blocked(screen)) callback.cancel();
    }
}
