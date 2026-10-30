package ru.he3hauka.toolvisuals.client.render.vignette;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import ru.he3hauka.toolvisuals.client.gui.render.UiSprites;

public final class VignetteOverlay {
    private static final int SIZE=256;
    private static final Identifier TEXTURE=Identifier.parse("toolvisuals:damage_vignette/mask");
    private static double previousWidth=-1,previousSoftness=-1;

    private VignetteOverlay() {}

    public static void draw(GuiGraphicsExtractor graphics,float x,float y,float width,float height,
                            int color,double opacity,double edgeWidth,double softness) {
        if (width<=0||height<=0||opacity<1d/255) return;
        if (previousWidth!=edgeWidth || previousSoftness!=softness) {
            var pixels=new NativeImage(SIZE,SIZE,false);
            for (int py=0;py<SIZE;py++) for (int px=0;px<SIZE;px++) {
                int alpha=(int)Math.round(VignetteMask.opacity(px/(SIZE-1d),py/(SIZE-1d),edgeWidth,softness)*255);
                pixels.setPixel(px,py,alpha<<24|0xffffff);
            }
            Minecraft.getInstance().getTextureManager().register(TEXTURE,new UiSprites.SmoothTexture("ToolVisuals damage vignette",pixels));
            previousWidth=edgeWidth;previousSoftness=softness;
        }
        int tint=(int)Math.round(Math.clamp(opacity,0,1)*255)<<24|color&0xffffff;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x,y);graphics.pose().scale(width/SIZE,height/SIZE);
        graphics.blit(RenderPipelines.GUI_TEXTURED,TEXTURE,0,0,0,0,SIZE,SIZE,SIZE,SIZE,tint);
        graphics.pose().popMatrix();
    }
}
