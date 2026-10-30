package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.world.VisualWorld;

@Mixin(LevelRenderer.class)
public abstract class VisualWorldRendererMixin {
    @Inject(method = "submitFeatures", at = @At("TAIL"))
    private void toolvisuals$worldVisuals(LevelRenderState state, SubmitNodeCollector nodes, boolean outlines, CallbackInfo ci) {
        VisualWorld.submit(state, nodes);
    }
}
