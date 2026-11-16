package ru.he3hauka.toolvisuals.client.items.highlight.render;

import java.nio.ByteBuffer;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.items.highlight.ItemGlowStyle;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.ITEM_HIGHLIGHT_HAND_RADIUS;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.ITEM_HIGHLIGHT_HAND_STRENGTH;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.ITEM_HIGHLIGHT_SHIMMER;

public final class ItemGlowControls {
    public final float radius, strength, shimmer, phase;
    public ItemGlowControls(float radius, float strength, float shimmer, float phase) {
        this.radius = radius;
        this.strength = strength;
        this.shimmer = shimmer;
        this.phase = phase;
    }
    public static ItemGlowControls capture(int height) {
        var config = ToolVisualsClient.CONFIG;
        return new ItemGlowControls(config.number(ITEM_HIGHLIGHT_HAND_RADIUS) * 6 * height / 1080f,
                config.number(ITEM_HIGHLIGHT_HAND_STRENGTH), config.flag(ITEM_HIGHLIGHT_SHIMMER) ? .18f : 0, ItemGlowStyle.phase());
    }
    public void write(ByteBuffer buffer) { buffer.putFloat(radius).putFloat(strength).putFloat(shimmer).putFloat(phase).flip(); }
}
