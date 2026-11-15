package ru.he3hauka.toolvisuals.mixin.itemhighlights;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.items.highlight.render.HandItemGlow;

@Mixin(GameRenderer.class)
public abstract class ItemGlowHandFrameMixin {
    @Inject(method = "renderItemInHand", at = @At("HEAD"))
    private void toolvisuals$begin(CallbackInfo callback) { HandItemGlow.begin(); }
    @Inject(method = "renderItemInHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;prepareFrame(Lnet/minecraft/client/renderer/SubmitNodeStorage;)Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;"))
    private void toolvisuals$mask(CallbackInfo callback) { HandItemGlow.mask((GameRenderer)(Object)this); }
    @Inject(method = "renderItemInHand", at = @At("RETURN"))
    private void toolvisuals$composite(CallbackInfo callback) { HandItemGlow.composite((GameRenderer)(Object)this); }
    @Inject(method = "close", at = @At("HEAD"))
    private void toolvisuals$close(CallbackInfo callback) { HandItemGlow.close(); }
}
