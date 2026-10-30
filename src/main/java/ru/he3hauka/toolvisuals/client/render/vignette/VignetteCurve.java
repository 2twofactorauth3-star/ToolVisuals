package ru.he3hauka.toolvisuals.client.render.vignette;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import ru.he3hauka.toolvisuals.cosmetic.style.ColorPalette;

public final class VignetteCurve {
    public record Band(double health, double opacity, ColorPalette color) {
        public Band {
            if (!Double.isFinite(health) || !Double.isFinite(opacity) || color==null) throw new IllegalArgumentException("Invalid health band");
            health=Math.clamp(health,.5,100);
            opacity=Math.clamp(opacity,0,1);
        }
    }
    public record Sample(int color, double opacity) {}
    private final List<Band> bands;
    private final double start;

    public VignetteCurve(double start, List<Band> bands) {
        if (!Double.isFinite(start) || bands.isEmpty()) throw new IllegalArgumentException("Missing health curve");
        var sorted=new ArrayList<>(bands);
        sorted.sort(Comparator.comparingDouble(Band::health).reversed().thenComparing(Comparator.comparingDouble(Band::opacity).reversed()));
        var unique=new ArrayList<Band>();
        for (var band:sorted) if (unique.isEmpty() || unique.getLast().health()!=band.health()) unique.add(band);
        this.bands=List.copyOf(unique);
        this.start=Math.max(start,unique.getFirst().health()+.5);
    }

    public Sample sample(double health, double seconds) {
        int previousColor=bands.getFirst().color().sample(seconds,0);
        if (!Double.isFinite(health) || health>=start) return new Sample(previousColor,0);
        double previousHealth=start,previousOpacity=0;
        for (var band:bands) {
            int color=band.color().sample(seconds,0);
            if (health>=band.health()) {
                double amount=Math.clamp((previousHealth-health)/(previousHealth-band.health()),0,1);
                amount=amount*amount*(3-2*amount);
                return new Sample(ColorPalette.mix(previousColor,color,amount,ColorPalette.Space.RGB),
                        previousOpacity+(band.opacity()-previousOpacity)*amount);
            }
            previousHealth=band.health();previousOpacity=band.opacity();previousColor=color;
        }
        return new Sample(previousColor,previousOpacity);
    }
}
