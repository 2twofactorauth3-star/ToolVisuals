package ru.he3hauka.toolvisuals.mixin.itemhighlights;

import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.items.highlight.render.HandItemGlow;

@Mixin(ModelFeatureRenderer.class)
public abstract class ItemGlowModelMaskMixin {
    @Inject(method = "prepareModel", at = @At("HEAD"), cancellable = true)
    private <S> void toolvisuals$outlineOnly(ModelFeatureRenderer.Submit<S> submit, CallbackInfo callback) { if (HandItemGlow.preparing() && !submit.renderType().isOutline()) callback.cancel(); }
}
