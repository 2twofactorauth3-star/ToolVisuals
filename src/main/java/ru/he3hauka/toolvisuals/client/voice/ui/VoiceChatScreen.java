package ru.he3hauka.toolvisuals.client.voice.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.voice.BundledVoiceChat;
import ru.he3hauka.toolvisuals.client.voice.VoiceChat;

public final class VoiceChatScreen extends VisualEditorScreen {
    private VisualsButton svcSettings,microphone,playback;
    public VoiceChatScreen(Screen parent){super(parent,text("title"));}
    private static Component text(String key,Object... args){return UiI18n.tr("toolvisuals.voice."+key,args);}
    @Override protected void init(){
        frame(620,392);var config=ToolVisualsClient.CONFIG;
        button(24,70,572,36,text("enabled"),UiPaint.Icon.KEYBOARD,()->config.setActive(VisualModule.VOICE_CHAT,!config.active(VisualModule.VOICE_CHAT)),()->config.active(VisualModule.VOICE_CHAT));
        microphone=button(24,122,278,36,text("microphone"),null,()->{if(VoiceChat.policy().enabled())config.setValue(VisualSetting.VOICE_MICROPHONE,config.flag(VisualSetting.VOICE_MICROPHONE)?0:1);},()->config.flag(VisualSetting.VOICE_MICROPHONE));
        playback=button(318,122,278,36,text("playback"),null,()->{if(VoiceChat.policy().enabled())config.setValue(VisualSetting.VOICE_PLAYBACK,config.flag(VisualSetting.VOICE_PLAYBACK)?0:1);},()->config.flag(VisualSetting.VOICE_PLAYBACK));
        svcSettings=button(24,294,572,34,text("svc_settings"),UiPaint.Icon.SETTINGS,()->BundledVoiceChat.settings(this),null);
        svcSettings.setTooltip(net.minecraft.client.gui.components.Tooltip.create(text("setup_hint")));
        button(24,346,278,30,text("controls"),UiPaint.Icon.KEYBOARD,()->minecraft.gui.setScreen(new KeyBindsScreen(this,minecraft.options)),null);
        button(318,346,278,30,UiI18n.tr("toolvisuals.editor.back"),UiPaint.Icon.BACK,this::onClose,null);
        updateButtons();
    }
    @Override protected void paint(GuiGraphicsExtractor g,int mx,int my){
        updateButtons();
        label(g,text("state_"+VoiceChat.state().name().toLowerCase(java.util.Locale.ROOT)),180,palette.accentText);
        label(g,text("setup"),216,palette.muted);label(g,text("settings_hint"),246,palette.muted);
    }
    private void updateButtons(){boolean enabled=VoiceChat.policy().enabled();microphone.active=playback.active=enabled;svcSettings.active=enabled&&minecraft.player!=null;}
    private void label(GuiGraphicsExtractor g,Component label,int y,int color){UiPaint.baseline(g,UiPaint.fit(g,label,572,.78f),px+24,UiPaint.textBaseline(g,py+y,28,.78f),.78f,color,1);}
}
