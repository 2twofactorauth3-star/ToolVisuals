package ru.he3hauka.toolvisuals.mixin.items;

import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.BakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public interface ItemLayerAccessor {
    @Accessor("model") BakedModel toolvisuals$model();
    @Invoker("transform") ItemTransform toolvisuals$transform();
}
