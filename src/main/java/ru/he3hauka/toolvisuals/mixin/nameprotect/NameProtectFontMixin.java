package ru.he3hauka.toolvisuals.mixin.nameprotect;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import ru.he3hauka.toolvisuals.client.nameprotect.NameProtect;

@Mixin(Font.class)
public abstract class NameProtectFontMixin {
    @ModifyVariable(method={"prepareText(Ljava/lang/String;FFIZI)Lnet/minecraft/client/gui/Font$PreparedText;",
            "width(Ljava/lang/String;)I"},at=@At("HEAD"),argsOnly=true,ordinal=0)
    private String toolvisuals$protectString(String text) { return NameProtect.text(text); }

    @ModifyVariable(method={"prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;",
            "prepare8xTextOutline", "width(Lnet/minecraft/util/FormattedCharSequence;)I"},at=@At("HEAD"),argsOnly=true,ordinal=0)
    private FormattedCharSequence toolvisuals$protectSequence(FormattedCharSequence text) { return NameProtect.text(text); }

    @ModifyVariable(method={"width(Lnet/minecraft/network/chat/FormattedText;)I","substrByWidth","wordWrapHeight","split","splitIgnoringLanguage"},
            at=@At("HEAD"),argsOnly=true,ordinal=0)
    private FormattedText toolvisuals$protectFormatted(FormattedText text) { return NameProtect.text(text); }
}
