package com.viaversion.viafabricplus.visuals.injection.mixin.strike_through_offset;

import net.minecraft.client.gui.Font;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Font.PreparedTextBuilder.class)
public abstract class MixinFont_PreparedTextBuilder {

    @Unique
    private static final float viaFabricPlusVisuals$offset = 0.5F;

    @ModifyArg(method = "accept(ILnet/minecraft/network/chat/Style;Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/glyphs/EffectGlyph;createEffect(FFFFFIIF)Lnet/minecraft/client/gui/font/TextRenderable;"), index = 1)
    private float fixStrikethroughMinY(float value) {
        if (ViaFabricPlus.getImpl().getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            return value - viaFabricPlusVisuals$offset;
        } else {
            return value;
        }
    }

    @ModifyArg(method = "accept(ILnet/minecraft/network/chat/Style;Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/glyphs/EffectGlyph;createEffect(FFFFFIIF)Lnet/minecraft/client/gui/font/TextRenderable;"), index = 3)
    private float fixStrikethroughMaxY(float value) {
        if (ViaFabricPlus.getImpl().getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            return value - viaFabricPlusVisuals$offset;
        } else {
            return value;
        }
    }

}
