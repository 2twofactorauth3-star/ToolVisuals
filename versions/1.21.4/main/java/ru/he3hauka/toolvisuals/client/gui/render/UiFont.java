package ru.he3hauka.toolvisuals.client.gui.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import ru.he3hauka.toolvisuals.client.gui.text.DisplayRasterState;
import ru.he3hauka.toolvisuals.mixin.gui.FontAccessor;

public final class UiFont {
    private static final ResourceLocation FACE=ResourceLocation.fromNamespaceAndPath("toolvisuals","ui");
    private static final DisplayRasterState DISPLAY=new DisplayRasterState();
    private static Font source,font;
    private static float capCenter=Float.NaN;
    private UiFont() {}
    public static Font get(){var vanilla=Minecraft.getInstance().font;if(source!=vanilla){var provider=((FontAccessor)(Object)vanilla).toolvisuals$fontProvider();font=new Font(id->provider.apply(FACE),false);source=vanilla;capCenter=Float.NaN;}return font;}
    public static boolean owns(Font candidate){return candidate!=null&&candidate==font;}
    public static void tickDisplay(Minecraft client){var window=client.getWindow();if(DISPLAY.update(window.getWidth(),window.getHeight(),window.getGuiScale(),client.isWindowActive())){UiText.clear();UiSprites.clear();}}
    public static TextBounds textBounds(Font candidate,String value){
        if(!owns(candidate))return new TextBounds(0,candidate.lineHeight);
        var provider=((FontAccessor)(Object)candidate).toolvisuals$fontProvider();
        var set=provider.apply(FACE);float top=Float.POSITIVE_INFINITY,bottom=Float.NEGATIVE_INFINITY;
        for(int point:(value.isBlank()?"H":value).codePoints().toArray()){
            if(Character.isWhitespace(point))continue;
            var glyph=(ru.he3hauka.toolvisuals.mixin.gui.BakedGlyphAccessor)(Object)set.getGlyph(point);
            top=Math.min(top,glyph.toolvisuals$top());bottom=Math.max(bottom,glyph.toolvisuals$bottom());
        }
        return Float.isFinite(top)&&Float.isFinite(bottom)?new TextBounds(top,bottom):new TextBounds(0,15);
    }
    public record TextBounds(float top,float bottom){public int selectionTop(int y){return (int)Math.floor(y+top)-1;}public int selectionBottom(int y){return (int)Math.ceil(y+bottom)+1;}}
    public static float centeredTextOffset(float height){if(Float.isNaN(capCenter)){var bounds=textBounds(get(),"H");capCenter=(bounds.top()+bounds.bottom())/2;}return Math.round(height/2-capCenter);}
    public static void invalidateMetrics(){capCenter=Float.NaN;}
}
