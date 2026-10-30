package com.viaversion.viafabricplus.visuals.injection.mixin.hud_element_changes;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.screen.ChatScreen;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ChatScreen.class)
public abstract class MixinChatScreen {

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;getIndicatorAt(DD)Lnet/minecraft/client/gui/hud/MessageIndicator;"))
    private MessageIndicator removeIndicator(ChatHud instance, double mouseX, double mouseY) {
        if (VisualSettings.INSTANCE.hideSignatureIndicator.isEnabled()) {
            return null;
        } else {
            return instance.getIndicatorAt(mouseX, mouseY);
        }
    }

}
