package ru.he3hauka.toolvisuals.client.gui.module;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.storage.JsonFile;

public final class SettingsSaveIndicator {
    private SettingsSaveIndicator(){}
    public static void draw(GuiGraphicsExtractor g,ClientConfig config,float x,float y,float width,UiPalette palette,float opacity){
        var status=config.persistenceStatus();if(status==JsonFile.Status.READY)return;
        var label=UiI18n.tr("toolvisuals.storage."+status.name().toLowerCase(java.util.Locale.ROOT));
        UiPaint.centered(g,UiPaint.fit(g,label,width,.7f),x,y,width,26,.7f,status==JsonFile.Status.DIRTY?0xffe9c47a:0xffff879c,opacity);
    }
}
