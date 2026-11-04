package ru.he3hauka.toolvisuals.mixin.potion;

import java.util.List;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.*;
import ru.he3hauka.toolvisuals.client.potion.liquid.*;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public abstract class PotionLayerMixin implements PotionLiquid.Layer {
    @Shadow @Final private List<BakedQuad> quads;
    public void toolvisuals$drain(float fill){var reduced=PotionGeometry.drain(List.copyOf(quads),fill);quads.clear();quads.addAll(reduced);}
}
