package com.viaversion.viafabricplus.injection.mixin.features.font;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.font.BuiltinEmptyGlyph;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.RenderableGlyph;
import net.minecraft.util.Identifier;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viafabricplus.features.font.filter_non_existing_characters.RenderableGlyphDiff;
import com.viaversion.viafabricplus.features.font.replace_blank_glyph.BuiltinEmptyGlyph1_12_2;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viafabricplus.settings.impl.DebugSettings;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FontStorage.class)
public abstract class MixinFontStorage {

    @Shadow
    private BakedGlyph blankBakedGlyph;
    @Shadow
    @Final
    private Identifier id;
    @Unique
    private BakedGlyph viaFabricPlus$blankBakedGlyph1_12_2;

    @Shadow
    protected abstract BakedGlyph bake(RenderableGlyph c);

    @Inject(method = "clear", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/BuiltinEmptyGlyph;bake(Ljava/util/function/Function;)Lnet/minecraft/client/font/BakedGlyph;", ordinal = 0))
    private void bakeBlankGlyph1_12_2(CallbackInfo ci) {
        this.viaFabricPlus$blankBakedGlyph1_12_2 = BuiltinEmptyGlyph1_12_2.INSTANCE.bake(this::bake);
    }

    @Inject(method = "findGlyph", at = @At("RETURN"), cancellable = true)
    private void filterGlyphs(int codePoint, CallbackInfoReturnable<FontStorage.GlyphPair> cir) {
        if (this.viaFabricPlusVisuals$shouldBeInvisible(codePoint)) {
            cir.setReturnValue(this.viaFabricPlus$getBlankGlyphPair());
        }
    }

    @Inject(method = "bake(I)Lnet/minecraft/client/font/BakedGlyph;", at = @At("RETURN"), cancellable = true)
    private void filterBakedGlyph(int codePoint, CallbackInfoReturnable<BakedGlyph> cir) {
        if (this.viaFabricPlusVisuals$shouldBeInvisible(codePoint)) {
            cir.setReturnValue(this.viaFabricPlus$getBlankBakedGlyph());
        }
    }

    @Inject(method = "findGlyph", at = @At("RETURN"), cancellable = true)
    private void fixBlankGlyph1_12_2(int codePoint, CallbackInfoReturnable<FontStorage.GlyphPair> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            final FontStorage.GlyphPair glyphPair = cir.getReturnValue();
            final Glyph glyph1 = glyphPair.glyph();
            final Glyph glyph2 = glyphPair.advanceValidatedGlyph();
            cir.setReturnValue(new FontStorage.GlyphPair(glyph1 == BuiltinEmptyGlyph.MISSING ? BuiltinEmptyGlyph1_12_2.INSTANCE : glyph1, glyph2 == BuiltinEmptyGlyph.MISSING ? BuiltinEmptyGlyph1_12_2.INSTANCE : glyph2));
        }
    }

    @Redirect(method = "bake(I)Lnet/minecraft/client/font/BakedGlyph;", at = @At(value = "FIELD", target = "Lnet/minecraft/client/font/FontStorage;blankBakedGlyph:Lnet/minecraft/client/font/BakedGlyph;"))
    private BakedGlyph fixBlankBakedGlyph1_12_2(FontStorage instance) {
        return this.viaFabricPlus$getBlankBakedGlyph();
    }

    @Unique
    private boolean viaFabricPlusVisuals$shouldBeInvisible(final int codePoint) {
        if (DebugSettings.INSTANCE.filterNonExistingGlyphs.getValue()) {
            return (this.id.equals(MinecraftClient.DEFAULT_FONT_ID) || this.id.equals(MinecraftClient.UNICODE_FONT_ID)) && !RenderableGlyphDiff.isGlyphRenderable(codePoint);
        } else {
            return false;
        }
    }

    @Unique
    private FontStorage.GlyphPair viaFabricPlus$getBlankGlyphPair() {
        if (ViaFabricPlus.getImpl().getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21_2)) {
            return new FontStorage.GlyphPair(BuiltinEmptyGlyph1_12_2.INSTANCE, BuiltinEmptyGlyph1_12_2.INSTANCE);
        } else {
            return FontStorage.GlyphPair.MISSING;
        }
    }

    @Unique
    private BakedGlyph viaFabricPlus$getBlankBakedGlyph() {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            return this.viaFabricPlus$blankBakedGlyph1_12_2;
        } else {
            return this.blankBakedGlyph;
        }
    }

}
