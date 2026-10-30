package ru.he3hauka.toolvisuals.mixin.localization;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.localization.SodiumLanguage;

@Mixin(Minecraft.class)
public abstract class SodiumLanguageMixin {
    @Shadow private Screen screen;

    @Inject(method = "setScreen", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;",
            opcode = org.objectweb.asm.Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void toolvisuals$sodiumLanguage(Screen next, CallbackInfo ci) {
        SodiumLanguage.screenChanged(screen);
    }
}
