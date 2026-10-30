package ru.he3hauka.toolvisuals.client.voice;

import de.maxhenkel.voicechat.gui.widgets.MicTestButton;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

public final class VoiceControls {
    private static final VoiceInputGate INPUT=new VoiceInputGate();
    private VoiceControls() {}
    public static boolean inputAllowed(){return INPUT.update(VoiceChat.policy().enabled(),VoiceControls::clearKeys);}
    private static void clearKeys(){
        var keys=KeyEvents.ALL_KEYS;if(keys==null)return;
        for(var key:keys)if(key!=null){key.setDown(false);while(key.consumeClick()){} }
    }
    public static boolean blockedScreen(Screen screen){return screen!=null&&screen.getClass().getName().startsWith("de.maxhenkel.voicechat.gui.")&&!VoiceChat.policy().enabled();}
    public static void closeDisabledScreen(Minecraft client){
        var screen=client.gui.screen();if(!blockedScreen(screen))return;
        stopTests(screen);screen.onClose();if(blockedScreen(client.gui.screen()))client.gui.setScreen(null);
    }
    private static void stopTests(GuiEventListener listener){
        if(listener instanceof MicTestButton test)test.stop();
        if(listener instanceof ContainerEventHandler container)for(var child:container.children())stopTests(child);
    }
}
