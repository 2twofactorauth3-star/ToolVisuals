package ru.he3hauka.toolvisuals.mixin.itemhighlights;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiGraphics.class)
public interface ItemGlowGuiAccessor {
    @Invoker("renderItem") void toolvisuals$renderItem(LivingEntity entity, Level level, ItemStack stack, int x, int y, int seed, int depth);
}
