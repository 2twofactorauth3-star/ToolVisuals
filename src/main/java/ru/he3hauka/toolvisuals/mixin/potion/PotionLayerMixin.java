package ru.he3hauka.toolvisuals.mixin.potion;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import org.spongepowered.asm.mixin.*;
import ru.he3hauka.toolvisuals.client.potion.liquid.*;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public abstract class PotionLayerMixin implements PotionLiquid.Layer {
    @Shadow private ItemQuads quads;
    public void toolvisuals$drain(float fill){quads=PotionGeometry.drain(quads,fill);}
}
