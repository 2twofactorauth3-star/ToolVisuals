package ru.he3hauka.toolvisuals.client.friends.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import ru.he3hauka.toolvisuals.client.friends.FriendData;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.players.PlayerListPanel;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

final class FriendRowDetails implements PlayerListPanel.Details {
    final FriendData data;
    final boolean health,armor,inventory;
    final int mode;
    private final String hp;
    FriendRowDetails(FriendData data,boolean health,boolean armor,boolean inventory,int mode){
        this.data=data;this.health=health;this.armor=armor;this.inventory=inventory;this.mode=mode;
        String value=data.healthKnown?number(data.health)+(data.absorption>0?" +"+number(data.absorption):""):"—";
        hp=UiI18n.tr("toolvisuals.friends_list.hp",value).getString();
    }
    private static String number(float value){return value==(int)value?Integer.toString((int)value):String.format(java.util.Locale.ROOT,"%.1f",value);}
    @Override public float rightWidth(GuiGraphicsExtractor g){return (health?UiPaint.textWidth(g,hp,.7f)+8:0)+(armor?56:0)+(inventory?16:0)+(inventory&&data.inventory.size()==37&&!data.inventory.get(36).isEmpty()?14:0);}
    @Override public float extraHeight(){return inventory&&mode==0?(data.inventory.size()==37?FriendInventoryPaint.HEIGHT+3:18):0;}
    @Override public float minimumWidth(){return inventory&&mode==0?FriendInventoryPaint.WIDTH+16:0;}
    @Override public void draw(GuiGraphicsExtractor g,UiPalette palette,float x,float y,float width){
        float cursor=x+width-5-rightWidth(g);
        if(health){
            float ratio=data.maximum>0?data.health/data.maximum:0;int color=!data.healthKnown?palette.muted:ratio>.65f?0xff7ee3a6:ratio>.3f?0xffffcf73:0xffff7589;
            UiPaint.baseline(g,hp,cursor,UiPaint.textBaseline(g,y,23,.7f),.7f,color,1);cursor+=UiPaint.textWidth(g,hp,.7f)+8;
        }
        if(armor){
            if(data.armor.size()==4)for(int i=0;i<4;i++)FriendInventoryPaint.small(g,palette,data.armor.get(i),cursor+i*14,y+5);
            else UiPaint.baseline(g,"—",cursor+19,UiPaint.textBaseline(g,y,23,.72f),.72f,palette.muted,1);
            cursor+=56;
        }
        if(inventory&&data.inventory.size()==37&&!data.inventory.get(36).isEmpty()){FriendInventoryPaint.small(g,palette,data.inventory.get(36),cursor,y+5);cursor+=14;}
        if(inventory)UiPaint.icon(g,UiPaint.Icon.HUD,cursor,y+5,12,data.inventory.size()==37?palette.accentText:palette.muted,1);
        if(inventory&&mode==0){
            if(data.inventory.size()==37)FriendInventoryPaint.grid(g,palette,data.inventory,x+(width-FriendInventoryPaint.WIDTH)/2,y+24);
            else UiPaint.baseline(g,UiPaint.fit(g,UiI18n.tr("toolvisuals.friends_list.inventory_unavailable"),width-10,.66f),x+5,UiPaint.textBaseline(g,y+23,18,.66f),.66f,palette.muted,1);
        }
    }
    void popup(GuiGraphicsExtractor g,UiPalette palette,double mouseX,double mouseY,float scale,String name){
        float width=FriendInventoryPaint.WIDTH+12,height=data.inventory.size()==37?FriendInventoryPaint.HEIGHT+28:47;
        g.pose().pushMatrix();g.pose().scale(scale,scale);
        float x=(float)Math.clamp(mouseX/scale+10,2,Math.max(2,g.guiWidth()/scale-width-2));
        float y=(float)Math.clamp(mouseY/scale+10,2,Math.max(2,g.guiHeight()/scale-height-2));
        UiPaint.rounded(g,x,y,width,height,6,palette.panel);
        UiPaint.baseline(g,UiPaint.fit(g,net.minecraft.network.chat.Component.literal(ru.he3hauka.toolvisuals.client.nameprotect.NameProtect.text(name)),width-12,.78f),x+6,UiPaint.textBaseline(g,y+2,20,.78f),.78f,palette.text,1);
        if(data.inventory.size()==37)FriendInventoryPaint.grid(g,palette,data.inventory,x+6,y+24);
        else UiPaint.baseline(g,UiPaint.fit(g,UiI18n.tr("toolvisuals.friends_list.inventory_unavailable"),width-12,.66f),x+6,UiPaint.textBaseline(g,y+22,20,.66f),.66f,palette.muted,1);
        g.pose().popMatrix();
    }
}
