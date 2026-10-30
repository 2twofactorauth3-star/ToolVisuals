package com.viaversion.viafabricplus.injection.mixin.features.limitation.max_chat_length;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;

import com.viaversion.viafabricplus.features.limitation.max_chat_length.MaxChatLength;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatScreen.class, priority = 1)
public abstract class MixinChatScreen {

    @Shadow
    protected TextFieldWidget chatField;

    @Inject(method = "init", at = @At("RETURN"))
    private void changeChatLength(CallbackInfo ci) {
        this.chatField.setMaxLength(MaxChatLength.getChatLength());
    }

}
