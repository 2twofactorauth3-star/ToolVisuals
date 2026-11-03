package ru.he3hauka.toolvisuals.client.friends.ui;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.items.SmoothHudItems;
import ru.he3hauka.toolvisuals.client.hud.inventory.InventoryHud;

final class FriendInventoryPaint {
    static final int WIDTH=164,HEIGHT=75;
    private FriendInventoryPaint(){}
    static void grid(GuiGraphicsExtractor g,UiPalette palette,List<ItemStack> items,float x,float y){
        for(int row=0;row<4;row++)for(int column=0;column<9;column++){
            int slot=InventoryHud.slot(row,column);float xx=x+column*18,yy=y+row*18+(row==3?2:0);
            UiPaint.rounded(g,xx,yy,17,17,2,UiPaint.alpha(palette.raised,.55f));
            var item=items.get(slot);if(!item.isEmpty()){SmoothHudItems.item(g,item,Math.round(xx),Math.round(yy));SmoothHudItems.decorations(g,item,Math.round(xx),Math.round(yy),palette);}
        }
    }
    static void small(GuiGraphicsExtractor g,UiPalette palette,ItemStack item,float x,float y){
        UiPaint.rounded(g,x,y,12,12,2,UiPaint.alpha(palette.raised,.45f));if(item.isEmpty())return;
        g.pose().pushMatrix();g.pose().translate(x,y);g.pose().scale(.75f,.75f);
        SmoothHudItems.item(g,item,0,0);SmoothHudItems.decorations(g,item,0,0,palette);g.pose().popMatrix();
    }
}
