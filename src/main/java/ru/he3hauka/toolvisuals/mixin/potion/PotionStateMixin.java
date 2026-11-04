package ru.he3hauka.toolvisuals.mixin.potion;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import ru.he3hauka.toolvisuals.client.potion.liquid.PotionLiquid;

@Mixin(ItemStackRenderState.class)
public interface PotionStateMixin extends PotionLiquid.Layers {
    @Accessor("layers") ItemStackRenderState.LayerRenderState[] toolvisuals$potionLayers();
    @Accessor("activeLayerCount") int toolvisuals$potionLayerCount();
}
