package ru.he3hauka.toolvisuals.client.optimization.frame;

import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.OPT_CHUNK_BUDGET;

public final class FrameWork {
    private static final FrameWorkBudget BUDGET = new FrameWorkBudget();
    private static long previous;
    private static boolean active;

    private FrameWork() {}

    public static void begin() {
        long now = System.nanoTime();
        long interval = previous == 0 ? 16_666_667L : now - previous;
        previous = now;
        BUDGET.begin(interval);
        active = Optimizations.enabled(OPT_CHUNK_BUDGET);
    }

    public static long chunks(long ceiling) { return active ? BUDGET.chunks(ceiling) : ceiling; }
    public static long textures(long ceiling) { return active ? BUDGET.textures(ceiling) : ceiling; }
    public static void consumed(long nanos, boolean texture) { if (active) BUDGET.consumed(nanos, texture); }
}
