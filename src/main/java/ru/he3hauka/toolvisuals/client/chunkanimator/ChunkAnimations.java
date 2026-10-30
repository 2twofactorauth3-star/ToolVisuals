package ru.he3hauka.toolvisuals.client.chunkanimator;

import java.nio.ByteBuffer;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.CHUNK_ANIMATOR;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class ChunkAnimations {
    private ChunkAnimations() { }
    public static boolean enabled() { return ToolVisualsClient.CONFIG.active(CHUNK_ANIMATOR); }
    public static long durationMillis() { return Math.round(ToolVisualsClient.CONFIG.value(CHUNK_ANIMATION_DURATION) * 1000); }
    public static boolean animateUpdates() { return enabled() && ToolVisualsClient.CONFIG.flag(CHUNK_ANIMATION_UPDATES); }
    public static float distance() {
        var config = ToolVisualsClient.CONFIG;
        int style = (int)config.value(CHUNK_ANIMATION_STYLE);
        return enabled() && style != 2 ? config.number(CHUNK_ANIMATION_DISTANCE) * (style == 0 ? -1 : 1) : 0;
    }
    public static float mode() {
        var config = ToolVisualsClient.CONFIG;
        return enabled() ? (config.flag(CHUNK_ANIMATION_SMOOTH) ? 1 : -1) * ((int)config.value(CHUNK_ANIMATION_STYLE) == 2 || config.flag(CHUNK_ANIMATION_FADE) ? 2 : 1) : 0;
    }

     
    public static void writeUniforms(ByteBuffer buffer, int base) {
         
        buffer.limit(Math.max(buffer.limit(), base + 192));
        if (enabled()) {
            buffer.putFloat(base + 176, 1f / durationMillis());
        }
        buffer.putFloat(base + 184, distance());
        buffer.putFloat(base + 188, mode());
    }
}
