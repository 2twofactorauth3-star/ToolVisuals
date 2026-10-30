package com.viaversion.viafabricplus.injection.mixin.compat.classic4j;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.StringHelper;

import com.viaversion.viafabricplus.injection.access.base.ITextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextFieldWidget.class)
public abstract class MixinTextFieldWidget implements ITextFieldWidget {

    @Unique
    private boolean viaFabricPlus$forbiddenCharactersUnlocked = false;

    @Redirect(method = "charTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringHelper;isValidChar(C)Z"))
    private boolean allowForbiddenCharacters(char c) {
        return this.viaFabricPlus$forbiddenCharactersUnlocked || StringHelper.isValidChar(c);
    }

    @Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringHelper;stripInvalidChars(Ljava/lang/String;)Ljava/lang/String;"))
    private String allowForbiddenCharacters(String string) {
        if (this.viaFabricPlus$forbiddenCharactersUnlocked) {
            return string;
        } else {
            return StringHelper.stripInvalidChars(string);
        }
    }

    @Override
    public void viaFabricPlus$unlockForbiddenCharacters() {
        this.viaFabricPlus$forbiddenCharactersUnlocked = true;
    }

}
