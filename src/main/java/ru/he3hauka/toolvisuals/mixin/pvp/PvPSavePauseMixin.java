package ru.he3hauka.toolvisuals.mixin.pvp;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.pvp.PvPSave;

@Mixin(PauseScreen.class)
public abstract class PvPSavePauseMixin {
    @Shadow private Button disconnectButton;
    @Unique private Component toolvisuals$disconnectLabel;
    @Inject(method={"init","tick"},at=@At("TAIL"))
    private void toolvisuals$combatLock(CallbackInfo ci) {
        if (disconnectButton == null) return;
        if (toolvisuals$disconnectLabel == null) toolvisuals$disconnectLabel = disconnectButton.getMessage();
        int seconds = PvPSave.remaining();
        disconnectButton.active = seconds == 0;
        disconnectButton.setMessage(seconds > 0 ? UiI18n.tr("toolvisuals.pvp_save.locked",seconds) : toolvisuals$disconnectLabel);
    }
}
