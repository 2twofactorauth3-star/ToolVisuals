package ru.he3hauka.toolvisuals.client.utility.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.hud.layout.HudEditorScreen;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.utility.chat.ui.ChatToolsScreen;
import ru.he3hauka.toolvisuals.client.utility.packs.ui.ResourcePacksScreen;
import ru.he3hauka.toolvisuals.client.utility.screenshots.ui.ScreenshotManagerScreen;

public final class ToolsScreen extends UtilityScreen {
    public ToolsScreen(Screen parent){super(parent,"tools");}
    @Override protected void init() {
        begin();
        button(24,70,278,52,ui("binds"),UiPaint.Icon.KEYBOARD,()->minecraft.gui.setScreen(new net.minecraft.client.gui.screens.options.controls.KeyBindsScreen(this,minecraft.options)),null);
        button(318,70,278,52,ui("screenshots"),UiPaint.Icon.CAMERA,()->minecraft.gui.setScreen(new ScreenshotManagerScreen(this)),null);
        button(24,134,278,52,ui("packs"),UiPaint.Icon.WORLD,()->minecraft.gui.setScreen(new ResourcePacksScreen(this)),null);
        if(ToolVisualsClient.CONFIG.available(ru.he3hauka.toolvisuals.client.module.VisualModule.CHAT_TABS))button(318,134,278,52,ui("chat"),UiPaint.Icon.PLAYER,()->minecraft.gui.setScreen(new ChatToolsScreen(this,false)),null);
        if(ToolVisualsClient.CONFIG.available(ru.he3hauka.toolvisuals.client.module.VisualModule.STREAMER_MODE))button(24,198,278,52,ui("privacy_names"),UiPaint.Icon.EYE,()->minecraft.gui.setScreen(new ChatToolsScreen(this,true)),null);
        if(ToolVisualsClient.CONFIG.available(ru.he3hauka.toolvisuals.client.connection.policy.ClientFeature.HUD_EDITOR))button(318,198,278,52,ui("hud_preview"),UiPaint.Icon.HUD,()->minecraft.gui.setScreen(new HudEditorScreen(this)),null).active=minecraft.player!=null;
        var config=ToolVisualsClient.CONFIG;
        button(24,290,278,40,ui("undo"),UiPaint.Icon.BACK,()->{config.undo();UiI18n.use(config.language());minecraft.gui.setScreen(new ToolsScreen(parent));},null).active=config.history.canUndo();
        button(318,290,278,40,ui("redo"),UiPaint.Icon.ARROW,()->{config.redo();UiI18n.use(config.language());minecraft.gui.setScreen(new ToolsScreen(parent));},null).active=config.history.canRedo();
    }
    @Override protected void paint(GuiGraphicsExtractor g,int mx,int my){label(g,ui("history_hint").getString(),24,342,572,.75f,palette.muted);footer(g);}
}
