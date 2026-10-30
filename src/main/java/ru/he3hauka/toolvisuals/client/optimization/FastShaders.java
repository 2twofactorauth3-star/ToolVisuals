package ru.he3hauka.toolvisuals.client.optimization;

import java.nio.ByteBuffer;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.FAST_SHADERS;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class FastShaders {
    public static final class Settings {
        public final int flags;
        private final long revision;
        public final float saturation,contrast,warmth;
        Settings(long revision,int flags,float saturation,float contrast,float warmth){this.revision=revision;this.flags=flags;this.saturation=saturation;this.contrast=contrast;this.warmth=warmth;}
    }
    private static volatile Settings cached;
    private FastShaders() {}
    public static Settings settings() {
        var config = ToolVisualsClient.CONFIG;
        long revision=config.revision();
        Settings previous=cached;
        if(previous!=null&&previous.revision==revision)return previous;
        boolean active = config.active(FAST_SHADERS), colors = active && config.flag(FAST_SHADER_COLORS)
                && (config.number(FAST_SHADER_SATURATION)!=1||config.number(FAST_SHADER_CONTRAST)!=1||config.number(FAST_SHADER_WARMTH)!=0);
        int flags=(active&&config.flag(FAST_SHADER_TEXTURES)?1:0)|(colors?2:0);
        Settings settings=new Settings(revision,flags,colors?config.number(FAST_SHADER_SATURATION):1,colors?config.number(FAST_SHADER_CONTRAST):1,colors?config.number(FAST_SHADER_WARMTH):0);
        cached=settings;
        return settings;
    }
    public static void writeUniforms(ByteBuffer buffer, int base) {
        buffer.limit(Math.max(buffer.limit(), base + 208));var settings=settings();
        buffer.putFloat(base + 192, settings.flags);
        buffer.putFloat(base + 196, settings.saturation);
        buffer.putFloat(base + 200, settings.contrast);
        buffer.putFloat(base + 204, settings.warmth);
    }
}
