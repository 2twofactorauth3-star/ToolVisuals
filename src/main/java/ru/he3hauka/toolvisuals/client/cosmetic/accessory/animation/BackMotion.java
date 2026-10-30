package ru.he3hauka.toolvisuals.client.cosmetic.accessory.animation;

import ru.he3hauka.toolvisuals.cosmetic.accessory.AccessoryLook;

public enum BackMotion {
    STILL(0,0,.55,0),
    FEATHERED(.14,.16,.55,1),
    LEATHERY(.12,.16,.55,1),
    BUTTERFLY(.30,.22,.85,1),
    CRYSTAL(.045,.12,.55,1),
    PHOENIX(.19,.18,.62,1),
    DRAGON(.15,.18,.48,1),
    DRAGONFLY(.21,.13,1.2,.8),
    MECHANICAL(.075,.10,.38,.65),
    ORBITAL(.012,.02,.22,.18),
    SWORDS(.012,.04,.45,.16);

    public final double amplitude,rest,frequency,flexibility;

    BackMotion(double amplitude,double rest,double frequency,double flexibility) {
        this.amplitude=amplitude;this.rest=rest;this.frequency=frequency;this.flexibility=flexibility;
    }

    public static BackMotion of(AccessoryLook.Back type) {
        return switch(type) {
            case NONE->STILL;
            case ANGEL->FEATHERED;
            case DEMON->LEATHERY;
            case BUTTERFLY->BUTTERFLY;
            case CRYSTAL->CRYSTAL;
            case PHOENIX->PHOENIX;
            case DRAGON->DRAGON;
            case DRAGONFLY->DRAGONFLY;
            case MECHANICAL->MECHANICAL;
            case ORBITAL->ORBITAL;
            case SWORDS->SWORDS;
        };
    }
}
