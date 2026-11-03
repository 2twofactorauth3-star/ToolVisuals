package ru.he3hauka.toolvisuals.client.navigation.gps;

import java.util.UUID;

public record GpsMark(UUID id, String context, String name, GpsTarget target, boolean active) {
    public GpsMark {
        if (id == null || context == null || context.isBlank() || context.length() > 2048
                || name == null || name.isBlank() || name.strip().length() > 40
                || name.chars().anyMatch(Character::isISOControl) || target == null)
            throw new IllegalArgumentException("Invalid GPS marker");
        name = name.strip();
    }
    public GpsMark active(boolean value) { return new GpsMark(id, context, name, target, value); }
}
