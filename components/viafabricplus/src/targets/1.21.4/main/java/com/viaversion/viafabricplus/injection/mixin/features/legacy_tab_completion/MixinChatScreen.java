package com.viaversion.viafabricplus.injection.mixin.features.legacy_tab_completion;

import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.viaversion.viafabricplus.settings.impl.DebugSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatScreen.class)
public abstract class MixinChatScreen {

    @Shadow
    protected TextFieldWidget chatField;

    @Shadow
    private String originalChatText;

    @Shadow
    ChatInputSuggestor chatInputSuggestor;

    @WrapWithCondition(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;setText(Ljava/lang/String;)V"))
    public boolean moveSetTextDown(TextFieldWidget instance, String text) {
        return !DebugSettings.INSTANCE.legacyTabCompletions.isEnabled();
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void moveSetTextDown(CallbackInfo ci) {
        if (DebugSettings.INSTANCE.legacyTabCompletions.isEnabled()) {
            this.chatField.setText(this.originalChatText);
            this.chatInputSuggestor.refresh();
        }
    }

    @Redirect(method = "onChatFieldUpdate", at = @At(value = "INVOKE", target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z"))
    private boolean fixCommandKey(String instance, Object other) {
        if (this.viaFabricPlus$keepTabComplete()) {
            return instance.equals(other);
        } else {
            return instance.isEmpty();
        }
    }

    @WrapWithCondition(method = "onChatFieldUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ChatInputSuggestor;refresh()V"))
    private boolean disableAutoTabComplete(ChatInputSuggestor instance) {
        return this.viaFabricPlus$keepTabComplete();
    }

    @Unique
    private boolean viaFabricPlus$keepTabComplete() {
        return !DebugSettings.INSTANCE.legacyTabCompletions.isEnabled() || !this.chatField.getText().startsWith("/");
    }

}
