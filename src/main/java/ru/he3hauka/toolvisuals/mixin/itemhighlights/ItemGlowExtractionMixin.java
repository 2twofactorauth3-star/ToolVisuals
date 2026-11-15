package ru.he3hauka.toolvisuals.mixin.itemhighlights;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import ru.he3hauka.toolvisuals.client.items.highlight.ItemHighlights;

@Mixin(GuiGraphicsExtractor.class)
public abstract class ItemGlowExtractionMixin {
    @WrapMethod(method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V")
    private void toolvisuals$itemGlow(LivingEntity owner, Level level, ItemStack stack, int x, int y, int seed, Operation<Void> original) {
        var previous = ItemHighlights.capture(stack);
        try {
            int color = ItemHighlights.slotColor();
            if (color >>> 24 != 0) ((GuiGraphicsExtractor)(Object)this).fill(x - 1, y - 1, x + 17, y + 17, color);
            original.call(owner, level, stack, x, y, seed);
        }
        finally { ItemHighlights.restore(previous); }
    }
}
