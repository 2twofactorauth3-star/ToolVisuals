package ru.he3hauka.toolvisuals.client.optimization.preset;

public final class GameOptionLimits {
    private GameOptionLimits() {}

    public static int simulationMinimum(int original) {
        return Math.min(original, 4);
    }
}
