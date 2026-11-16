package ru.he3hauka.toolvisuals.mixin.itemhighlights;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.items.highlight.ItemHighlights;
import ru.he3hauka.toolvisuals.client.items.highlight.render.ItemGlowPipeline;
import ru.he3hauka.toolvisuals.client.items.highlight.render.ItemGlowQuad;

@Mixin(GuiRenderer.class)
public abstract class ItemGlowGuiRendererMixin {
    @Shadow @Final private GuiRenderState renderState;
    @Shadow private GpuTextureView itemsAtlasView;
    @Inject(method = "submitBlitFromItemAtlas", at = @At("HEAD"))
    private void toolvisuals$glow(GuiItemRenderState item, float u0, float v0, int pixels, int atlasPixels, CallbackInfo callback) {
        var style = ((ItemHighlights.GuiState)(Object)item).toolvisuals$itemGlow();
        if (style == null || style.color() >>> 24 == 0) return;
        int padding = (int)Math.ceil(style.radius() + 1);
        float span = (float)pixels / atlasPixels, pad = padding * span / 16;
        var glow = new BlitRenderState(ItemGlowPipeline.GUI,
                TextureSetup.singleTexture(itemsAtlasView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)),
                new Matrix3x2f(item.pose()), item.x() - padding, item.y() - padding, item.x() + 16 + padding, item.y() + 16 + padding,
                u0 - pad, u0 + span + pad, v0 + pad, v0 - span - pad, style.color(), item.scissorArea());
        ((ItemGlowQuad.State)(Object)glow).toolvisuals$itemGlowQuad(new ItemGlowQuad(Math.round((u0 + span * .5f) * atlasPixels), Math.round((v0 - span * .5f) * atlasPixels), pixels, style.radius() * pixels / 16, style.phase()));
        renderState.submitBlitToCurrentLayer(glow);
    }
}
