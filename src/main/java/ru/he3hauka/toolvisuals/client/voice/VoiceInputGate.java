package ru.he3hauka.toolvisuals.client.voice;

public final class VoiceInputGate {
    private boolean enabled;
    public boolean update(boolean active,Runnable clear){
        if(!active||!enabled)clear.run();
        enabled=active;
        return active;
    }
}
