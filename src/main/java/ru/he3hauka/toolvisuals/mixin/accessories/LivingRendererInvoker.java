package ru.he3hauka.toolvisuals.mixin.accessories;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntityRenderer.class)
public interface LivingRendererInvoker {
    @Invoker("addLayer") boolean toolvisuals$addLayer(RenderLayer<?,?> layer);
}
