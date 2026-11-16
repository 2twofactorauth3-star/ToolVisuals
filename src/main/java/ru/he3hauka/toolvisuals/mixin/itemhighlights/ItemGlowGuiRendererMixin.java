package ru.he3hauka.toolvisuals.mixin.itemhighlights;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
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
    @Inject(method = "submitBlitFromItemAtlas", at = @At("HEAD"))
    private void toolvisuals$glow(GuiItemRenderState item, GuiItemAtlas.SlotView slot, CallbackInfo callback) {
        var style = ((ItemHighlights.GuiState)(Object)item).toolvisuals$itemGlow();
        if (style == null || style.color() >>> 24 == 0) return;
        float pixels = (slot.u1() - slot.u0()) * slot.textureView().getWidth(0);
        int padding = (int)Math.ceil(style.radius() + 1);
        float du = padding * (slot.u1() - slot.u0()) / 16, dv = padding * (slot.v1() - slot.v0()) / 16;
        var glow = new BlitRenderState(ItemGlowPipeline.GUI,
                TextureSetup.singleTexture(slot.textureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)),
                new Matrix3x2f(item.pose()), item.x() - padding, item.y() - padding, item.x() + 16 + padding, item.y() + 16 + padding,
                slot.u0() - du, slot.u1() + du, slot.v0() - dv, slot.v1() + dv, style.color(), item.scissorArea());
        ((ItemGlowQuad.State)(Object)glow).toolvisuals$itemGlowQuad(new ItemGlowQuad(
                Math.round((slot.u0() + slot.u1()) * .5f * slot.textureView().getWidth(0)),
                Math.round((slot.v0() + slot.v1()) * .5f * slot.textureView().getHeight(0)), pixels, style.radius() * pixels / 16, style.phase()));
        renderState.addBlitToCurrentLayer(glow);
    }
}
