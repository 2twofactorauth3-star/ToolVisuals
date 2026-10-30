package ru.he3hauka.toolvisuals.client.hud.health;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.layout.HudElement;
import ru.he3hauka.toolvisuals.client.hud.layout.HudPlacement;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.HEALTH_INDICATOR;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class HealthIndicator {
    private static final HitHealthState STATE=new HitHealthState();
    private static Object world;
    private static Player target;
    private static int healthNumber=-1,absorptionNumber=-1;
    private static String label="",shield="";
    private HealthIndicator() {}
    private static double now(){return System.nanoTime()/50_000_000d;}

    public static void attacked(Player attacker,Entity entity){
        var client=Minecraft.getInstance();
        if(!ToolVisualsClient.CONFIG.active(HEALTH_INDICATOR)||client.level==null||attacker!=client.player)return;
        if(!(entity instanceof Player victim)||victim==attacker||victim.level()!=client.level){target=null;STATE.clear();return;}
        world=client.level;target=victim;
        STATE.hit(now(),Math.round(ToolVisualsClient.CONFIG.number(HEALTH_INDICATOR_DURATION)*20));
        STATE.update(victim.getHealth(),victim.getMaxHealth(),victim.getAbsorptionAmount());
    }

    public static void tick(Minecraft client){
        if(client.level!=world||client.player==null||!ToolVisualsClient.CONFIG.active(HEALTH_INDICATOR)
                ||!STATE.active(now())){world=client.level;target=null;STATE.clear();}
    }

    public static void draw(GuiGraphicsExtractor graphics,UiPalette palette){
        var client=Minecraft.getInstance();var config=ToolVisualsClient.CONFIG;
        boolean preview=HudPlacement.preview();
        if(!preview&&(world!=client.level||!STATE.active(now())))return;
        if(!preview&&target!=null)STATE.update(target.getHealth(),target.getMaxHealth(),target.getAbsorptionAmount());
        int hp=HealthStyle.number(preview?6:STATE.health());
        int nextShield=config.flag(HEALTH_INDICATOR_ABSORPTION)?HealthStyle.number(preview?2:STATE.absorption()):0;
        if(healthNumber!=hp||absorptionNumber!=nextShield){
            healthNumber=hp;absorptionNumber=nextShield;
            label=String.valueOf(hp);shield=nextShield==0?"":" +"+nextShield;
        }
        float scale=HealthStyle.scale(hp,config.flag(HEALTH_INDICATOR_LOW_SCALE),config.number(HEALTH_INDICATOR_SCALE),config.number(HEALTH_INDICATOR_CRITICAL_SCALE));
        float textScale=scale*.6f,padding=config.flag(HEALTH_INDICATOR_BACKGROUND)?3:0;
        float textWidth=UiPaint.textWidth(graphics,label+shield,textScale);
        float width=textWidth+padding*2,height=11*scale+padding*2;
        float x=graphics.guiWidth()*HudPlacement.value(HEALTH_INDICATOR_X)/100-width/2;
        float y=graphics.guiHeight()*HudPlacement.value(HEALTH_INDICATOR_Y)/100-height/2;
        graphics.pose().pushMatrix();
        HudPlacement.place(graphics,HudElement.HIT_HEALTH,x,y,width,height);
        if(config.flag(HEALTH_INDICATOR_BACKGROUND))UiPaint.rounded(graphics,x,y,width,height,4,palette.hudPanel);
        float baseline=UiPaint.textBaseline(graphics,y,height,textScale);
        UiPaint.baseline(graphics,label,x+padding+.5f,baseline+.5f,textScale,0xff000000,.45f);
        UiPaint.baseline(graphics,label,x+padding,baseline,textScale,0xff000000|config.color(HealthStyle.color(hp)),1);
        if(!shield.isEmpty())UiPaint.baseline(graphics,shield,x+padding+UiPaint.textWidth(graphics,label,textScale),baseline,textScale,0xffffaa00,1);
        graphics.pose().popMatrix();graphics.pose().popMatrix();
    }
}
