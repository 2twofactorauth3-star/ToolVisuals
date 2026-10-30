package ru.he3hauka.toolvisuals.mixin.nameprotect;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;

import ru.he3hauka.toolvisuals.client.nameprotect.NameProtect;

@Mixin(EditBox.class)
public abstract class NameProtectEditBoxMixin {
    @WrapMethod(method="extractWidgetRenderState")
    private void toolvisuals$rawEditor(GuiGraphicsExtractor g,int mx,int my,float partial,Operation<Void> original) {
        NameProtect.beginRawText();
        try { original.call(g,mx,my,partial); } finally { NameProtect.endRawText(); }
    }
    @WrapMethod(method="updateTextPosition")
    private void toolvisuals$rawPosition(Operation<Void> original) {
        NameProtect.beginRawText();
        try { original.call(); } finally { NameProtect.endRawText(); }
    }
    @WrapMethod(method="getScreenX")
    private int toolvisuals$rawCursor(int index,Operation<Integer> original) {
        NameProtect.beginRawText();
        try { return original.call(index); } finally { NameProtect.endRawText(); }
    }
}
