package ru.he3hauka.toolvisuals.mixin.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GlyphRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.gui.render.UiPipelines;

@Mixin(GlyphRenderState.class)
public abstract class UiGlyphRenderStateMixin {
    @Shadow @Final private TextRenderable renderable;

    @Inject(method = "textureSetup", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$filterUiFont(CallbackInfoReturnable<TextureSetup> callback) {
        if (toolvisuals$isUiFont()) {
            callback.setReturnValue(TextureSetup.singleTextureWithLightmap(renderable.textureView(),
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)));
        }
    }

    @Inject(method = "pipeline", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$blendUiFont(CallbackInfoReturnable<RenderPipeline> callback) {
        if (toolvisuals$isUiFont() && renderable.guiPipeline() == RenderPipelines.GUI_TEXT_GRAYSCALE) {
            callback.setReturnValue(UiPipelines.TEXT);
        }
    }

    private boolean toolvisuals$isUiFont() {
        String label = renderable.textureView().texture().getLabel();
        return label != null && (label.startsWith("toolvisuals:ui/")
                || ru.he3hauka.toolvisuals.client.hud.tab.font.TabFontRaster.ownsAtlas(label));
    }
}
