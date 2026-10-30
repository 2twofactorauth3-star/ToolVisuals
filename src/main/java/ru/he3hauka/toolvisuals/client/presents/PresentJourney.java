package ru.he3hauka.toolvisuals.client.presents;

import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import ru.he3hauka.toolvisuals.network.world.WorldContextWire.Area;

public final class PresentJourney {
    public static final String ARENA_COMMAND = "warp pvp";
    private Object connection;
    private String context = "";
    private boolean sent;

    public void scope(Object connection, String context) {
        Objects.requireNonNull(context);
        if (context.isEmpty() && connection != null && this.connection == connection) return;
        if (this.connection != connection || !this.context.equals(context)) {
            this.connection = connection;
            this.context = context;
            sent = false;
        }
    }

    public boolean travel(Area area, Set<Integer> collected, BooleanSupplier send) {
        if (context.isEmpty() || connection == null || sent || area != Area.SPAWN
                || !PresentCatalog.spawnComplete(collected)
                || PresentCatalog.ARENA.stream().allMatch(point -> collected.contains(point.id()))) return false;
        if (!send.getAsBoolean()) return false;
        sent = true;
        return true;
    }
}
