package com.viaversion.viafabricplus.visuals.injection.mixin.potion_glint;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PotionItem.class)
public abstract class MixinPotionItem extends Item {

    public MixinPotionItem(final Properties settings) {
        super(settings);
    }

    @Override
    public boolean isFoil(final ItemStack stack) {
        if (VisualSettings.INSTANCE.potionEnchantmentGlint.isEnabled()) {
            return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).hasEffects();
        } else {
            return super.isFoil(stack);
        }
    }

}
