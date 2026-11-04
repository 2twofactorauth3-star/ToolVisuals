package ru.he3hauka.toolvisuals.mixin.potion;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.BakedModel;
import org.spongepowered.asm.mixin.*;
import ru.he3hauka.toolvisuals.client.potion.liquid.*;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public abstract class PotionLayerMixin implements PotionLiquid.Layer {
    @Shadow private BakedModel model;
    public void toolvisuals$drain(float fill){if(model!=null)model=PotionGeometry.drain(model,fill);}
}
