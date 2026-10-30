package ru.he3hauka.toolvisuals.client.render.vignette.ui;

import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.color.ColorEditorScreen;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.render.vignette.VignetteCurve;
import ru.he3hauka.toolvisuals.client.render.vignette.VignetteOverlay;
import ru.he3hauka.toolvisuals.client.render.vignette.VignettePulse;
import ru.he3hauka.toolvisuals.client.render.vignette.VignetteSettings;
import ru.he3hauka.toolvisuals.cosmetic.style.ColorPalette;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.DAMAGE_VIGNETTE;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class DamageVignetteScreen extends VisualEditorScreen {
    private final VignettePulse pulse=new VignettePulse();
    private double previewHealth=7;
    private long revision=-1;
    private VignetteCurve curve;

    public DamageVignetteScreen(Screen parent) { super(parent,Component.literal(DAMAGE_VIGNETTE.title)); }
    private static Component text(String key,Object...args) { return UiI18n.tr("toolvisuals.vignette."+key,args); }

    @Override protected void init() {
        frame(820,590);
        var config=ToolVisualsClient.CONFIG;
        button(20,62,152,30,text("enabled"),UiPaint.Icon.CHECK,
                ()->config.setActive(DAMAGE_VIGNETTE,!config.active(DAMAGE_VIGNETTE)),()->config.active(DAMAGE_VIGNETTE));
        button(184,62,272,30,VIGNETTE_MODE.formatted(config.value(VIGNETTE_MODE)),null,()->{
            config.setValue(VIGNETTE_MODE,(config.value(VIGNETTE_MODE)+1)%3);rebuildWidgets();
        },null);
        button(468,62,332,30,VIGNETTE_ABSORPTION.label(),null,
                ()->config.setValue(VIGNETTE_ABSORPTION,config.flag(VIGNETTE_ABSORPTION)?0:1),()->config.flag(VIGNETTE_ABSORPTION));
        slider(20,330,360,text("preview_health"),0,100,.5,()->previewHealth,value->previewHealth=value,DamageVignetteScreen::hp);
        for (int index=0;index<VignetteSettings.LEVELS.size();index++) {
            var level=VignetteSettings.LEVELS.get(index);int y=108+index*110;
            setting(414,y+43,174,level.health());setting(608,y+43,176,level.opacity());
            button(686,y+7,98,28,text("color"),null,()->minecraft.gui.setScreen(new ColorEditorScreen(this,level.color().label(),
                    config.colorStyle(level.color()),style->config.setColorStyle(level.color(),style))),null);
        }
        button(20,370,172,28,text("test_hit"),null,()->pulse.trigger(System.nanoTime()),null);
        button(208,370,172,28,text("critical"),null,()->previewHealth=config.value(VIGNETTE_HP_3),null);
        setting(20,412,172,VIGNETTE_WIDTH);setting(208,412,172,VIGNETTE_SOFTNESS);
        setting(20,458,172,VIGNETTE_TRANSITION);setting(208,458,172,VIGNETTE_FLASH_DURATION);
        setting(414,447,370,VIGNETTE_START_HP);
        setting(414,493,370,VIGNETTE_FLASH_STRENGTH);
        button(20,542,360,30,tr("back"),UiPaint.Icon.BACK,this::onClose,null);
        button(400,542,400,30,UiI18n.tr("toolvisuals.reset"),UiPaint.Icon.RESET,()->{config.reset(DAMAGE_VIGNETTE);rebuildWidgets();},null);
    }

    private void setting(int x,int y,int width,VisualSetting setting) {
        var config=ToolVisualsClient.CONFIG;
        Component label=setting.name().startsWith("VIGNETTE_HP_")?text("health"):
                setting.name().startsWith("VIGNETTE_OPACITY_")?text("intensity"):setting.label();
        slider(x,y,width,label,setting.min,setting.max,setting.step,()->config.value(setting),
                value->config.setValue(setting,value),value->{
                    if(setting==VIGNETTE_START_HP||setting.name().startsWith("VIGNETTE_HP_"))return hp(value);
                    if(setting==VIGNETTE_TRANSITION||setting==VIGNETTE_FLASH_DURATION)return String.format(Locale.ROOT,"%.2f s",value);
                    return Math.round(value*100)+"%";
                });
    }

    private static String hp(double value) { return String.format(Locale.ROOT,"%.1f HP",value); }

    @Override protected void paint(GuiGraphicsExtractor graphics,int mouseX,int mouseY) {
        var config=ToolVisualsClient.CONFIG;
        if(curve==null||revision!=config.revision()){curve=VignetteSettings.curve(config);revision=config.revision();}
        int x=px+20,y=py+108;
        UiPaint.rounded(graphics,x,y,360,204,8,0xff14262e);
        graphics.enableScissor(x,y,x+360,y+204);
        graphics.fill(x,y+115,x+360,y+204,0xff263c35);
        for(int column=0;column<7;column++) {
            int bx=x+16+column*55,by=y+90+(column%3)*12;
            graphics.fill(bx,by,bx+40,by+45,0xff344c46);
            graphics.fill(bx+3,by+3,bx+37,by+6,0xff456058);
        }
        var sample=curve.sample(previewHealth,ColorPalette.clock());
        double amount=config.value(VIGNETTE_MODE)==1?0:sample.opacity();
        if(config.value(VIGNETTE_MODE)!=0) amount=Math.max(amount,pulse.value(System.nanoTime(),config.value(VIGNETTE_FLASH_DURATION))*
                Math.max(sample.opacity(),config.value(VIGNETTE_FLASH_STRENGTH)));
        VignetteOverlay.draw(graphics,x,y,360,204,sample.color(),amount,config.value(VIGNETTE_WIDTH),config.value(VIGNETTE_SOFTNESS));
        graphics.disableScissor();
        UiPaint.text(graphics,text("preview"),x+14,y+12,.8f,0xffe5eaf1,1);
        UiPaint.centered(graphics,hp(previewHealth),x,y+79,360,30,1.1f,0xfff3f5fa,1);
        UiPaint.centered(graphics,text("preview_strength",Math.round(amount*100)).getString(),x,y+111,360,22,.75f,0xffc8d3df,1);
        UiPaint.text(graphics,text("hp_hint"),px+20,py+512,.72f,palette.muted,1);
        for(int index=0;index<VignetteSettings.LEVELS.size();index++) {
            int top=py+108+index*110;var level=VignetteSettings.LEVELS.get(index);
            UiPaint.rounded(graphics,px+400,top,400,100,7,palette.field);
            UiPaint.text(graphics,text("level",index+1),px+414,top+13,.85f,palette.text,1);
            UiPaint.rounded(graphics,px+662,top+12,16,16,5,0xff000000|config.color(level.color()));
        }
    }
}
