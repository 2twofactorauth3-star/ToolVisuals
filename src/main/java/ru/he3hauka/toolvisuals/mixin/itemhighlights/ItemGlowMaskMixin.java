package ru.he3hauka.toolvisuals.mixin.itemhighlights;

import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.items.highlight.render.HandItemGlow;

@Mixin(ItemFeatureRenderer.class)
public abstract class ItemGlowMaskMixin {
    @Inject(method = "prepareMainSubmit", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$outlineOnly(CallbackInfo callback) { if (HandItemGlow.preparing()) callback.cancel(); }
}
