package ru.he3hauka.toolvisuals.client.voice;

public record VoicePolicy(boolean enabled,boolean microphone,boolean playback) {
    public boolean transmit(){return enabled&&microphone;}
    public boolean receive(){return enabled&&playback;}
}
