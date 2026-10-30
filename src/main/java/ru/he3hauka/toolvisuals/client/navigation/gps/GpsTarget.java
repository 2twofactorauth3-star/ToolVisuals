package ru.he3hauka.toolvisuals.client.navigation.gps;

import java.util.UUID;

public record GpsTarget(double x, double y, double z, String world) {
    public GpsTarget {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                || Math.abs(x) > 30_000_000 || Math.abs(z) > 30_000_000 || Math.abs(y) > 4096
                || !ru.he3hauka.toolvisuals.network.world.WorldContextWire.validName(world)) throw new IllegalArgumentException("Invalid GPS target");
    }

    public static GpsTarget parse(String arguments) {
        String[] values = arguments.strip().split("\\s+", 4);
        if (values.length < 3) throw new IllegalArgumentException("Expected X Y Z [world]");
        return new GpsTarget(Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]),
                values.length == 4 ? values[3] : "");
    }

    public boolean matches(String dimension, String serverWorld, UUID worldId) {
        if (world.isEmpty()) return true;
        if (world.equalsIgnoreCase(serverWorld) || worldId != null && world.equalsIgnoreCase(worldId.toString())) return true;
        return world.equalsIgnoreCase(dimension) || !world.contains(":") && ("minecraft:" + world).equalsIgnoreCase(dimension);
    }
}
