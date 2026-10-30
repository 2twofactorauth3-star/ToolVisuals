package com.viaversion.viafabricplus.features.font.replace_blank_glyph;

import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.RenderableGlyph;

import java.util.function.Function;

public enum BuiltinEmptyGlyph1_12_2 implements Glyph {
    INSTANCE;

    private static final int WIDTH = 0;
    private static final int HEIGHT = 8;

    @Override
    public float getAdvance() {
        return WIDTH;
    }

    @Override
    public BakedGlyph bake(Function<RenderableGlyph, BakedGlyph> glyphRendererGetter) {
        return glyphRendererGetter.apply(new RenderableGlyph() {

            @Override
            public int getWidth() {
                return WIDTH;
            }

            @Override
            public int getHeight() {
                return HEIGHT;
            }

            @Override
            public float getOversample() {
                return 1F;
            }

            @Override
            public void upload(int x, int y) {
            }

            @Override
            public boolean hasColor() {
                return true;
            }
        });
    }

}
