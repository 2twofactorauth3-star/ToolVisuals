package ru.he3hauka.toolvisuals.client.hand.ui;

import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.hand.HandProperty;
import ru.he3hauka.toolvisuals.client.hand.HandSide;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.CUSTOM_HAND;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.HAND_CHAT_EDITOR;

public final class HandSettingsScreen extends VisualEditorScreen {
    private HandSide selected;

    public HandSettingsScreen(Screen parent){this(parent,HandSide.RIGHT);}
    public HandSettingsScreen(Screen parent,HandSide selected){super(parent,Component.literal("CustomHand"));this.selected=selected;}
    private static Component text(String key,Object...args){return HandEditorOverlay.text(key,args);}

    @Override protected void init() {
        frame(780,580);var config=ToolVisualsClient.CONFIG;
        button(20,64,132,30,text("right"),null,()->select(HandSide.RIGHT),()->selected==HandSide.RIGHT);
        button(160,64,132,30,text("left"),null,()->select(HandSide.LEFT),()->selected==HandSide.LEFT);
        button(316,64,220,30,HAND_CHAT_EDITOR.label(),null,()->config.setValue(HAND_CHAT_EDITOR,config.flag(HAND_CHAT_EDITOR)?0:1),()->config.flag(HAND_CHAT_EDITOR));
        button(548,64,212,30,text("enabled"),UiPaint.Icon.CHECK,()->config.setActive(CUSTOM_HAND,!config.active(CUSTOM_HAND)),()->config.active(CUSTOM_HAND));
        for(int index=0;index<3;index++) {
            control(20+252*index,130,HandProperty.values()[index]);
            control(20+252*index,212,new HandProperty[]{HandProperty.ROTATION_X,HandProperty.ROTATION_Y,HandProperty.ROTATION_Z}[index]);
            control(20+252*index,354,new HandProperty[]{HandProperty.STRETCH_X,HandProperty.STRETCH_Y,HandProperty.STRETCH_Z}[index]);
        }
        control(20,290,HandProperty.SCALE);
        var editor=button(272,290,488,32,text("open_editor"),UiPaint.Icon.RENDER,()->{
            config.setActive(CUSTOM_HAND,true);minecraft.gui.setScreen(new HandEditorScreen(this,selected));
        },null);editor.active=minecraft.level!=null&&minecraft.player!=null;
        var flags=new HandProperty[]{HandProperty.NO_SWING,HandProperty.NO_EQUIP,HandProperty.HIDE};
        for(int index=0;index<flags.length;index++) {
            var property=flags[index];var setting=property.setting(selected);
            button(20+252*index,422,236,32,text(property.name().toLowerCase(Locale.ROOT)),null,
                    ()->config.setValue(setting,config.flag(setting)?0:1),()->config.flag(setting));
        }
        button(20,474,362,32,text("copy"),null,()->HandProperty.copy(config,selected,false),null);
        button(398,474,362,32,text("mirror"),null,()->HandProperty.copy(config,selected,true),null);
        button(20,530,236,30,text("reset_selected"),UiPaint.Icon.RESET,()->{HandProperty.reset(config,selected);rebuildWidgets();},null);
        button(272,530,236,30,text("reset_both"),null,()->{HandProperty.reset(config,HandSide.RIGHT);HandProperty.reset(config,HandSide.LEFT);rebuildWidgets();},null);
        button(524,530,236,30,tr("back"),UiPaint.Icon.BACK,this::onClose,null);
    }
    private void select(HandSide side){selected=side;rebuildWidgets();}
    private void control(int x,int y,HandProperty property) {
        var config=ToolVisualsClient.CONFIG;var setting=property.setting(selected);
        slider(x,y,236,text(property.name().toLowerCase(Locale.ROOT)),setting.min,setting.max,setting.step,()->config.value(setting),value->config.setValue(setting,value),value->{
            if(property==HandProperty.SCALE)return String.format(Locale.ROOT,"%.2f×",value);
            if(property.name().startsWith("ROTATION"))return String.format(Locale.ROOT,"%.1f°",value);
            if(property.name().startsWith("STRETCH"))return Math.round(value*100)+"%";
            return String.format(Locale.ROOT,"%.3f",value);
        });
    }
    @Override protected void paint(GuiGraphicsExtractor graphics,int mouseX,int mouseY) {
        UiPaint.text(graphics,text("position"),px+20,py+108,.85f,palette.accentText,1);
        UiPaint.text(graphics,text("rotation"),px+20,py+190,.85f,palette.accentText,1);
        UiPaint.text(graphics,text("size"),px+20,py+268,.85f,palette.accentText,1);
        UiPaint.text(graphics,text("axis_scale"),px+20,py+332,.85f,palette.muted,1);
        UiPaint.text(graphics,text("separate_hint"),px+20,py+394,.75f,palette.muted,1);
    }
}
