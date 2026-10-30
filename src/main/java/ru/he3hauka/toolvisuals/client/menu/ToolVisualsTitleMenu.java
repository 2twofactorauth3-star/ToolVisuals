package ru.he3hauka.toolvisuals.client.menu;

import java.util.*;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.connection.ConnectionVersions;
import ru.he3hauka.toolvisuals.client.connection.ui.AccountScreen;
import ru.he3hauka.toolvisuals.client.connection.ui.ConnectionScreen;
import ru.he3hauka.toolvisuals.client.gui.animation.Motion;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.gui.surface.UiSurface;
import ru.he3hauka.toolvisuals.client.gui.text.UiTextStyle;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

public final class ToolVisualsTitleMenu {
    public final List<TitleMenuButton> widgets;
    private final TitleMenuLayout layout;
    private final List<Button> nativeButtons;
    private final Map<TitleMenuButton,TitleMenuLayout.Box> positions=new LinkedHashMap<>();
    private final Motion entrance=new Motion(0);
    private final MenuSpotlight spotlight=new MenuSpotlight();
    private long lastFrame=System.nanoTime();
    private double age;
    public ToolVisualsTitleMenu(Screen screen,List<Button> nativeButtons,int width,int height,int gameScale,Runnable switchMenu,Runnable rebuild) {
        layout=new TitleMenuLayout(width,height,gameScale);this.nativeButtons=List.copyOf(nativeButtons);
        nativeAction("menu.singleplayer","menu.playdemo","singleplayer",UiPaint.Icon.PLAYER,layout.singleplayer,true);
        nativeAction("menu.multiplayer",null,"multiplayer",UiPaint.Icon.WORLD,layout.multiplayer,true);
        var client=Minecraft.getInstance();
        add("accounts",UiPaint.Icon.PLAYER,()->client.getUser().getName(),layout.accounts,false,()->client.gui.setScreen(new AccountScreen(screen)));
        add("versions",UiPaint.Icon.VERSIONS,ConnectionVersions::label,layout.versions,false,()->client.gui.setScreen(new ConnectionScreen(screen,true)));
        nativeAction("menu.options",null,"settings",UiPaint.Icon.SETTINGS,layout.settings,false);
        nativeAction("menu.online",null,"realms",UiPaint.Icon.REALMS,layout.realms,false);
        nativeAction("menu.quit",null,"quit",UiPaint.Icon.CLOSE,layout.quit,false);
        add("switch",UiPaint.Icon.RESET,null,layout.switchMenu,false,switchMenu).setTooltip(Tooltip.create(tr("switch_vanilla")));
        var theme=add("theme",UiPaint.Icon.SUN,null,layout.theme,false,()->{ToolVisualsClient.CONFIG.setLightTheme(!ToolVisualsClient.CONFIG.lightTheme());rebuild.run();});
        theme.setTooltip(Tooltip.create(tr("theme")));
        var language=add("language",null,null,layout.language,false,()->{var next=ToolVisualsClient.CONFIG.language().next();ToolVisualsClient.CONFIG.setLanguage(next);UiI18n.use(next);rebuild.run();});
        language.setMessage(Component.literal(ToolVisualsClient.CONFIG.language().badge));language.setTooltip(Tooltip.create(tr("language")));
        widgets=List.copyOf(positions.keySet());position(0);
    }
    private static Component tr(String key) { return UiI18n.tr("toolvisuals.main."+key); }
    private TitleMenuButton add(String label,UiPaint.Icon icon,Supplier<String> detail,TitleMenuLayout.Box box,boolean primary,Runnable action) {
        var button=new TitleMenuButton(tr(label),icon,detail,layout.scale,primary,action);positions.put(button,box);return button;
    }
    private void nativeAction(String key,String fallback,String label,UiPaint.Icon icon,TitleMenuLayout.Box box,boolean primary) {
        var original=TitleMenuButtons.find(nativeButtons,key);if(original==null)original=TitleMenuButtons.find(nativeButtons,fallback);
        var target=original;
        var button=add(label,icon,null,box,primary,()->{if(target!=null&&target.active)target.onPress(null);});
        button.active=target!=null&&target.active;
        if(!button.active)button.setTooltip(Tooltip.create(tr("unavailable")));
    }
    private void position(float slide) {
        positions.forEach((button,box)->{
            button.setX(Math.round(box.x*layout.scale));button.setY(Math.round((box.y+(box==layout.switchMenu||box==layout.language||box==layout.theme?0:slide))*layout.scale));
            button.setWidth(Math.round(box.width*layout.scale));button.setHeight(Math.round(box.height*layout.scale));
        });
    }
    public void draw(GuiGraphicsExtractor g,int mx,int my,float partial) {
        long now=System.nanoTime();float elapsed=Math.clamp((now-lastFrame)/1e9f,0,.1f);lastFrame=now;
        age+=elapsed;
        float visible=entrance.step(1,elapsed,9),slide=(1-visible)*10;
        boolean light=ToolVisualsClient.CONFIG.lightTheme();
        var palette=new UiPalette(light?1:0);
        g.fill(0,0,g.guiWidth(),g.guiHeight(),UiPaint.alpha(light?0x79e8e4f4:0x690a0713,.85f+.15f*visible));
        g.pose().pushMatrix();g.pose().scale(layout.scale,layout.scale);
        var box=layout.panel;float y=box.y+slide;
        spotlight.update(box.x,y,box.width,box.height,mx/layout.scale,my/layout.scale,elapsed);
        UiPaint.material(g,UiSurface.GLASS,light,box.x,y,box.width,box.height,1,visible);
        spotlight.draw(g,box.x,y,box.width,box.height,light,visible);
        float icon=layout.compact?28:32,brandScale=layout.compact?1.65f:1.9f;
        float brandWidth=UiPaint.textWidth(g,TitleBrand.TEXT,brandScale,UiTextStyle.BRAND),groupWidth=brandWidth+icon+12;
        float left=box.x+(box.width-groupWidth)/2,top=y+(layout.compact?15:21);
        UiPaint.material(g,UiSurface.EMBLEM,light,left,top,icon,icon,1,visible);
        UiPaint.icon(g,UiPaint.Icon.RENDER,left+6,top+6,icon-12,palette.accentText,visible);
        float baseline=UiPaint.textBaseline(g,top,icon,brandScale,UiTextStyle.BRAND),textX=left+icon+12;
        UiPaint.coloredBaseline(g,TitleBrand.TEXT,textX,baseline,brandScale,UiTextStyle.BRAND,position->TitleBrand.color(position,age,light),visible);
        UiPaint.centered(g,tr("subtitle").getString(),box.x,y+(layout.compact?47:60),box.width,18,.72f,palette.muted,visible,UiTextStyle.MENU);
        UiPaint.rounded(g,box.x+box.width/2-18,y+(layout.compact?68:80),36,1.5f,.75f,UiPaint.alpha(palette.accent,visible*.85f));
        g.pose().popMatrix();
        position(slide);
        for(var button:widgets){button.frame(palette,light,visible,elapsed);button.extractRenderState(g,mx,my,partial);}
    }
}
