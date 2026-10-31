package ru.he3hauka.toolvisuals.mixin.gui;

import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.compatibility.render.RasterPipelines;
import ru.he3hauka.toolvisuals.client.hud.tab.font.TabFontRaster;

@Mixin(GlyphRenderTypes.class)
public abstract class GlyphRenderTypesMixin {
    @Inject(method = "createForIntensityTexture", at = @At("RETURN"), cancellable = true)
    private static void toolvisuals$antialiasedGlyphs(ResourceLocation texture, CallbackInfoReturnable<GlyphRenderTypes> callback) {
        String atlas = texture.toString();
        if (!atlas.startsWith("toolvisuals:ui/") && !TabFontRaster.ownsAtlas(atlas)) return;
        var original = callback.getReturnValue();
        callback.setReturnValue(new GlyphRenderTypes(RasterPipelines.glyph(original.normal()),
                RasterPipelines.glyph(original.seeThrough()), RasterPipelines.glyph(original.polygonOffset())));
    }
}
