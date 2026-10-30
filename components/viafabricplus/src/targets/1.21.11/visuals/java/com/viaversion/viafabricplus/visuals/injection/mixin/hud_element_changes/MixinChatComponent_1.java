package com.viaversion.viafabricplus.visuals.injection.mixin.hud_element_changes;

import net.minecraft.client.GuiMessageTag;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$1")
public abstract class MixinChatComponent_1 {

    @ModifyExpressionValue(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/GuiMessage$Line;tag()Lnet/minecraft/client/GuiMessageTag;"))
    private GuiMessageTag removeIndicator(GuiMessageTag original) {
        if (VisualSettings.INSTANCE.hideSignatureIndicator.isEnabled()) {
            return null;
        } else {
            return original;
        }
    }

}
