package ru.he3hauka.toolvisuals.protocols.client;

import java.util.Objects;
import java.util.function.Consumer;

public final class ProtocolMenus {
    private static volatile Consumer<Object> opener;

    private ProtocolMenus() {}

    public static void install(Consumer<Object> screenOpener) {
        opener = Objects.requireNonNull(screenOpener);
    }

    public static void open(Object parent) {
        var current = opener;
        if (current == null) throw new IllegalStateException("ToolVisuals protocol menu is not initialized");
        current.accept(parent);
    }
}
