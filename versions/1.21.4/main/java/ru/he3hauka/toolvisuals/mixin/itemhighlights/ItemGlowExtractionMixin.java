package ru.he3hauka.toolvisuals.mixin.itemhighlights;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import ru.he3hauka.toolvisuals.client.items.highlight.ItemHighlights;
import ru.he3hauka.toolvisuals.client.items.highlight.render.LegacyGuiGlow;

@Mixin(GuiGraphics.class)
public abstract class ItemGlowExtractionMixin {
    @WrapMethod(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V")
    private void toolvisuals$itemGlow(LivingEntity owner, Level level, ItemStack stack, int x, int y, int seed, int depth, Operation<Void> original) {
        if (!LegacyGuiGlow.capturing()) {
            var previous = ItemHighlights.capture(stack);
            try { LegacyGuiGlow.draw((GuiGraphics)(Object)this, owner, level, stack, x, y, seed, ItemHighlights.extracting()); }
            finally { ItemHighlights.restore(previous); }
        }
        original.call(owner, level, stack, x, y, seed, depth);
    }
}
