package com.viaversion.viafabricplus.visuals.injection.mixin.hud_element_changes;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatHud.class)
public abstract class MixinChatHud {

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private MessageIndicator removeIndicator(MessageIndicator instance) {
        return VisualSettings.INSTANCE.hideSignatureIndicator.isEnabled() ? null : instance;
    }

}
