package ru.he3hauka.toolvisuals.client.voice;

import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.replay.ui.ReplayViewerScreen;

public final class VoiceChat {
    public enum State { MISSING, INITIALIZING, DISCONNECTED, DISABLED, MUTED, TALKING, READY, ERROR }
    private static volatile Supplier<State> backend;
    private VoiceChat(){}
    public static void attach(Supplier<State> provider){backend=provider;}
    public static VoicePolicy policy(){var config=ToolVisualsClient.CONFIG;var client=Minecraft.getInstance();boolean replay=client!=null&&client.gui!=null&&client.gui.screen() instanceof ReplayViewerScreen;return new VoicePolicy(config.active(VisualModule.VOICE_CHAT)&&!replay,config.flag(VisualSetting.VOICE_MICROPHONE),config.flag(VisualSetting.VOICE_PLAYBACK));}
    public static State state(){
        if(!FabricLoader.getInstance().isModLoaded("voicechat"))return State.MISSING;
        if(!policy().enabled())return State.DISABLED;
        var provider=backend;if(provider==null)return State.INITIALIZING;
        try{return provider.get();}catch(RuntimeException|LinkageError error){ClientErrors.report("Read voice chat backend state",error);return State.ERROR;}
    }
}
