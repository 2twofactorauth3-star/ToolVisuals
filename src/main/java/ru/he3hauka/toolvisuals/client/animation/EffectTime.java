package ru.he3hauka.toolvisuals.client.animation;

public final class EffectTime {
    private EffectTime(){}
    public static double at(long tick,float partial){return (double)tick+(double)partial;}
}
