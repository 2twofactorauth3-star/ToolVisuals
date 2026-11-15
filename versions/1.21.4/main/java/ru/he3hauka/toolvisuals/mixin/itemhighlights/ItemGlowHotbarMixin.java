package ru.he3hauka.toolvisuals.mixin.itemhighlights;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import ru.he3hauka.toolvisuals.client.items.highlight.ItemHighlights;

@Mixin(Hud.class)
public abstract class ItemGlowHotbarMixin {
    @WrapMethod(method = "renderItemHotbar")
    private void toolvisuals$hotbar(GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        boolean previous = ItemHighlights.hotbar();
        try { original.call(graphics, delta); }
        finally { ItemHighlights.restoreHotbar(previous); }
    }
}
