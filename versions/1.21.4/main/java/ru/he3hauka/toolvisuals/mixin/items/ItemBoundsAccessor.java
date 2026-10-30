package ru.he3hauka.toolvisuals.mixin.items;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackRenderState.class)
public interface ItemBoundsAccessor {
    @Accessor("layers") ItemStackRenderState.LayerRenderState[] toolvisuals$layers();
    @Accessor("activeLayerCount") int toolvisuals$count();
    @Accessor("isLeftHand") boolean toolvisuals$leftHand();
}
