package com.viaversion.viafabricplus.injection.mixin.features.legacy_tab_completion;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.CommandSuggestions.SuggestionsList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import com.viaversion.viafabricplus.settings.impl.DebugSettings;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandSuggestions.class)
public abstract class MixinCommandSuggestions {

    @Shadow
    public abstract void updateCommandInfo();

    @Shadow
    @Nullable
    private CommandSuggestions.@Nullable SuggestionsList suggestions;

    @Shadow
    @Final
    EditBox input;

    @Shadow
    @Final
    private List<FormattedCharSequence> commandUsage;

    @Inject(method = "formatChat", at = @At(value = "HEAD"), cancellable = true)
    private void disableTextFieldColors(String original, int firstCharacterIndex, CallbackInfoReturnable<FormattedCharSequence> cir) {
        if (this.viaFabricPlus$cancelTabComplete()) {
            cir.setReturnValue(FormattedCharSequence.forward(original, Style.EMPTY));
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void handle1_12_2KeyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        if (this.viaFabricPlus$cancelTabComplete()) {
            if (input.key() == GLFW.GLFW_KEY_TAB && this.suggestions == null) {
                this.updateCommandInfo();
            } else if (this.suggestions != null) {
                if (this.suggestions.keyPressed(input)) {
                    cir.setReturnValue(true);
                    return;
                }
                this.input.setSuggestion(null);
                this.suggestions = null;
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void clearMessages(GuiGraphics drawContext, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.viaFabricPlus$cancelTabComplete()) {
            this.commandUsage.clear();
        }
    }

    @Unique
    private boolean viaFabricPlus$cancelTabComplete() {
        return DebugSettings.INSTANCE.legacyTabCompletions.isEnabled() && this.input.getValue().startsWith("/");
    }

}
