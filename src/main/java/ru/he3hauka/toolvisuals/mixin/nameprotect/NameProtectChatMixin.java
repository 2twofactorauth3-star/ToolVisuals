package ru.he3hauka.toolvisuals.mixin.nameprotect;

import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.FormattedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import ru.he3hauka.toolvisuals.client.nameprotect.NameProtect;

@Mixin(ComponentRenderUtils.class)
public abstract class NameProtectChatMixin {
    @ModifyVariable(method="wrapComponents",at=@At("HEAD"),argsOnly=true,ordinal=0)
    private static FormattedText toolvisuals$protectBeforeWrapping(FormattedText text) { return NameProtect.text(text); }
}
