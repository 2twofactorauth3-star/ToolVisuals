package com.viaversion.viafabricplus.visuals.injection.mixin.hud_element_changes;

import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public abstract class MixinChatComponent {

    @ModifyVariable(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private GuiMessageTag removeIndicator(GuiMessageTag instance) {
        return VisualSettings.INSTANCE.hideSignatureIndicator.isEnabled() ? null : instance;
    }

}
