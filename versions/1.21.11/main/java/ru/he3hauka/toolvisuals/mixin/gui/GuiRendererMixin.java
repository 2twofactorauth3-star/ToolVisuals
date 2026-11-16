package ru.he3hauka.toolvisuals.mixin.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.gui.render.UiClip;
import ru.he3hauka.toolvisuals.client.hud.items.SmoothHudItems;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @ModifyExpressionValue(method = "submitBlitFromItemAtlas",
            at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/textures/FilterMode;NEAREST:Lcom/mojang/blaze3d/textures/FilterMode;"))
    private FilterMode toolvisuals$smoothHudSampler(
            FilterMode original,
            @Local(argsOnly = true) GuiItemRenderState item) {
        return ((SmoothHudItems.State)(Object)item).toolvisuals$smoothItem()
                ? FilterMode.LINEAR : original;
    }
    @ModifyExpressionValue(method = "prepareItemElements", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/render/GuiRenderer;getGuiScaleInvalidatingItemAtlasIfChanged()I"))
    private int toolvisuals$itemResolution(int original) { return Math.max(2, original); }
    @Inject(method = "executeDraw", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$skipEmptyClip(@Coerce Object draw, RenderPass pass, GpuBuffer buffer, VertexFormat.IndexType indexType, CallbackInfo ci) {
        var clip = ((GuiDrawAccessor)draw).toolvisuals$scissorArea();
        if (clip == null) return;
        var window = Minecraft.getInstance().getWindow();
         
        if (UiClip.empty(clip.left(), clip.top(), clip.right(), clip.bottom(), window.getGuiScale(), window.getWidth(), window.getHeight())) ci.cancel();
    }
}
