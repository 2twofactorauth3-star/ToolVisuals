package ru.he3hauka.toolvisuals.client.chunkanimator;

import java.nio.FloatBuffer;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniform;
import org.lwjgl.opengl.GL20;

public final class RegionUniform extends GlUniform<FloatBuffer> {
    public RegionUniform(int index){super(index);}
    @Override public void set(FloatBuffer values){GL20.glUniform1fv(index,values);}
}
