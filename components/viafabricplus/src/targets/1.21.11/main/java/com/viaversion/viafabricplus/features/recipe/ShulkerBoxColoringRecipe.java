package com.viaversion.viafabricplus.features.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.CustomRecipe.Serializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;


public final class ShulkerBoxColoringRecipe extends CustomRecipe {

    public static final RecipeSerializer<ShulkerBoxColoringRecipe> SERIALIZER = new Serializer<>(ShulkerBoxColoringRecipe::new);

    public ShulkerBoxColoringRecipe(CraftingBookCategory craftingRecipeCategory) {
        super(craftingRecipeCategory);
    }

    public boolean matches(CraftingInput input, Level world) {
        int i = 0;
        int j = 0;

        for (int k = 0; k < input.size(); k++) {
            ItemStack stack = input.getItem(k);
            if (!stack.isEmpty()) {
                if (Block.byItem(stack.getItem()) instanceof ShulkerBoxBlock) {
                    i++;
                } else {
                    if (!(stack.getItem() instanceof DyeItem)) {
                        return false;
                    }

                    j++;
                }

                if (j > 1 || i > 1) {
                    return false;
                }
            }
        }

        return i == 1 && j == 1;
    }

    public ItemStack assemble(CraftingInput input, HolderLookup.Provider wrapperLookup) {
        ItemStack result = ItemStack.EMPTY;
        DyeItem dyeItem = (DyeItem) Items.WHITE_DYE;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                if (Block.byItem(item) instanceof ShulkerBoxBlock) {
                    result = stack;
                } else if (item instanceof DyeItem) {
                    dyeItem = (DyeItem) item;
                }
            }
        }

        return result.transmuteCopy(ShulkerBoxBlock.getBlockByColor(dyeItem.getDyeColor()), 1);
    }

    @Override
    public RecipeSerializer<ShulkerBoxColoringRecipe> getSerializer() {
        return SERIALIZER;
    }

}
