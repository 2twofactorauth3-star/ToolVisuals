package ru.he3hauka.toolvisuals.client.voice;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.*;
import de.maxhenkel.voicechat.api.internal.events.HudIconRenderEvent;
import de.maxhenkel.voicechat.api.internal.events.StartMicEvent;

public final class SimpleVoicePlugin implements VoicechatPlugin {
    private final java.util.function.Supplier<VoicePolicy> policies;
    private volatile VoicechatClientApi client;
    public SimpleVoicePlugin(){this(VoiceChat::policy);}
    SimpleVoicePlugin(java.util.function.Supplier<VoicePolicy> policies){this.policies=policies;}
    @Override public String getPluginId(){return "toolvisuals";}
    @Override public void registerEvents(EventRegistration events){
        events.registerEvent(ClientVoicechatInitializationEvent.class,event->{client=event.getVoicechat();VoiceChat.attach(this::state);});
        events.registerEvent(StartMicEvent.class,event->{if(!policies.get().enabled())event.cancel();},Integer.MAX_VALUE);
        events.registerEvent(HudIconRenderEvent.class,event->{if(!policies.get().enabled())event.cancel();},Integer.MAX_VALUE);
        events.registerEvent(NameTagIconRenderEvent.class,event->{if(!policies.get().enabled())event.cancel();},Integer.MAX_VALUE);
        events.registerEvent(ClientSoundEvent.class,event->{if(!policies.get().transmit()&&!event.cancel())silence(event.getRawAudio());},Integer.MAX_VALUE);
        events.registerEvent(ClientReceiveSoundEvent.EntitySound.class,this::receive,Integer.MAX_VALUE);
        events.registerEvent(ClientReceiveSoundEvent.LocationalSound.class,this::receive,Integer.MAX_VALUE);
        events.registerEvent(ClientReceiveSoundEvent.StaticSound.class,this::receive,Integer.MAX_VALUE);
    }
    private void receive(ClientReceiveSoundEvent event){if(!policies.get().receive())silence(event.getRawAudio());}
    private static void silence(short[] samples){if(samples!=null)java.util.Arrays.fill(samples,(short)0);}
    private VoiceChat.State state(){
        var api=client;if(api==null)return VoiceChat.State.INITIALIZING;
        if(api.isDisconnected())return VoiceChat.State.DISCONNECTED;if(api.isDisabled())return VoiceChat.State.DISABLED;
        if(!policies.get().microphone()||api.isMuted())return VoiceChat.State.MUTED;
        return api.isTalking()?VoiceChat.State.TALKING:VoiceChat.State.READY;
    }
}
