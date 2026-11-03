package ru.he3hauka.toolvisuals.mixin.gui;

import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BakedGlyph.class)
public interface BakedGlyphAccessor {
    @Accessor("up") float toolvisuals$top();
    @Accessor("down") float toolvisuals$bottom();
}
