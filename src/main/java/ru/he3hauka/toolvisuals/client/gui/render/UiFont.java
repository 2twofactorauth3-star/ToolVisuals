package ru.he3hauka.toolvisuals.client.gui.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

import ru.he3hauka.toolvisuals.mixin.gui.FontAccessor;

public final class UiFont {
    private static final FontDescription FACE = new FontDescription.Resource(
            Identifier.fromNamespaceAndPath("toolvisuals", "ui"));
    private static Font source;
    private static Font font;
    private static float capCenter = Float.NaN;
    private static final ru.he3hauka.toolvisuals.client.gui.text.DisplayRasterState DISPLAY=new ru.he3hauka.toolvisuals.client.gui.text.DisplayRasterState();

    private UiFont() {
    }

    public static Font get() {
        Font vanilla = Minecraft.getInstance().font;
        if (source != vanilla) {
            Font.Provider provider = ((FontAccessor) (Object) vanilla).toolvisuals$fontProvider();
            font = new Font(new Font.Provider() {
                @Override
                public GlyphSource glyphs(FontDescription description) {
                    return provider.glyphs(FACE);
                }

                @Override
                public EffectGlyph effect() {
                    return provider.effect();
                }
            });
            source = vanilla;
            capCenter = Float.NaN;
        }
        return font;
    }

    public static void invalidateMetrics(){capCenter=Float.NaN;}

    public static boolean owns(Font candidate){return candidate!=null&&candidate==font;}
    public static void tickDisplay(Minecraft client){
        var window=client.getWindow();
        if(DISPLAY.update(window.getWidth(),window.getHeight(),window.getGuiScale(),client.isWindowActive())){UiText.clear();UiSprites.clear();}
    }

    public static TextBounds textBounds(Font candidate,String value){
        float[] bounds={Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY};
        candidate.prepareText(value.isBlank()?"HgЙруф":value,0,0,0xffffffff,false,0).visit(new Font.GlyphVisitor(){
            @Override public void acceptGlyph(net.minecraft.client.gui.font.TextRenderable.Styled glyph){
                bounds[0]=Math.min(bounds[0],glyph.top());bounds[1]=Math.max(bounds[1],glyph.bottom());
            }
        });
        return Float.isFinite(bounds[0])&&Float.isFinite(bounds[1])?new TextBounds(bounds[0],bounds[1]):new TextBounds(0,15);
    }

    public record TextBounds(float top,float bottom){
        public int selectionTop(int textY){return (int)Math.floor(textY+top)-1;}
        public int selectionBottom(int textY){return (int)Math.ceil(textY+bottom)+1;}
    }

     
    public static float centeredTextOffset(float height) {
        Font current = get();
        if (Float.isNaN(capCenter)) {
            current.prepareText("H", 0, 0, 0xffffffff, false, 0).visit(new Font.GlyphVisitor() {
                @Override public void acceptGlyph(net.minecraft.client.gui.font.TextRenderable.Styled glyph) {
                    capCenter = (glyph.top() + glyph.bottom()) / 2;
                }
            });
            if (Float.isNaN(capCenter)) capCenter = current.lineHeight / 2f;
        }
        return Math.round(height / 2 - capCenter);
    }
}
