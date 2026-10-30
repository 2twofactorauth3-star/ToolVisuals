package ru.he3hauka.toolvisuals.client.voice;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.onboarding.OnboardingManager;
import de.maxhenkel.voicechat.voice.client.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import ru.he3hauka.toolvisuals.mixin.voicechat.VoiceMicInvoker;

public final class BundledVoiceChat {
    private static final VoiceSession SESSION=new VoiceSession();
    private static final class Engine implements VoiceSession.Engine {
        private final ClientVoicechat client;
        private VoicePolicy previous;
        Engine(ClientVoicechat client){this.client=client;}
        @Override public Object identity(){return client.getConnection();}
        @Override public boolean connected(){return client.getConnection()!=null&&client.getConnection().isConnected();}
        @Override public boolean hasMicrophone(){return client.getMicThread()!=null;}
        @Override public void startMicrophone(){((VoiceMicInvoker)client).toolvisuals$startMic(client.getConnection());}
        @Override public void stopMicrophone(){client.closeMicThread();}
        @Override public void synchronize(VoicePolicy policy){
            var states=ClientManager.getPlayerStateManager();var config=VoicechatClient.CLIENT_CONFIG;
            if(policy.enabled()){
                if((previous==null||!previous.enabled())&&config.disabled.get())states.setDisabled(false);
                if(config.muted.get()==policy.microphone())states.setMuted(!policy.microphone());
            }
            else if(client.getRecorder()!=null)client.setRecording(false);
            previous=policy;
            states.syncOwnState();
        }
    }
    private static ClientVoicechat current;
    private static Engine engine;
    private BundledVoiceChat(){}
    public static void tick(Minecraft minecraft){
        VoiceControls.inputAllowed();
        VoiceControls.closeDisabledScreen(minecraft);
        var client=ClientManager.getClient();if(client!=current){current=client;engine=client==null?null:new Engine(client);}
        SESSION.update(engine,VoiceChat.policy());
    }
    public static void settings(Screen parent){
        if(!VoiceChat.policy().enabled())return;
        if(OnboardingManager.isOnboarding())OnboardingManager.startOnboarding(parent);
        else Minecraft.getInstance().gui.setScreen(new de.maxhenkel.voicechat.gui.VoiceChatSettingsScreen(parent));
    }
}
