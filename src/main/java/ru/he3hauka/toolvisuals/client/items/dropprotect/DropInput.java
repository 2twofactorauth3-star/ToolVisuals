package ru.he3hauka.toolvisuals.client.items.dropprotect;

import java.util.ArrayDeque;

import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public final class DropInput {
    private static final ArrayDeque<Long> WORLD = new ArrayDeque<>();
    private static long sequence, dropPress, mousePress;
    private static Object screen, connection;

    private DropInput() {}

    public static void keyPressed(Minecraft client, KeyEvent event) {
        synchronize(client);
        if (client.options.keyDrop.matches(event)) boundPress(client, ++sequence);
    }

    public static void mousePressed(Minecraft client, MouseButtonEvent event) {
        synchronize(client);
        mousePress = ++sequence;
        if (client.options.keyDrop.matchesMouse(event)) boundPress(client, mousePress);
    }

    private static void boundPress(Minecraft client, long input) {
        dropPress = input;
        if (client.gui.screen() == null && client.player != null && client.player.isAlive() && !client.player.isSpectator()) {
            if (WORLD.size() == 16) WORLD.removeFirst();
            WORLD.addLast(input);
        }
    }

    public static void synchronize(Minecraft client) {
        if (screen != client.gui.screen() || connection != client.getConnection()) {
            WORLD.clear();
            screen = client.gui.screen();
            connection = client.getConnection();
        }
        if (client.player == null || !client.player.isAlive()) WORLD.clear();
    }

    public static long worldPress() { return WORLD.isEmpty() ? dropPress : WORLD.removeFirst(); }
    public static long inventoryPress(boolean cursor) { return cursor ? mousePress : dropPress; }
}
