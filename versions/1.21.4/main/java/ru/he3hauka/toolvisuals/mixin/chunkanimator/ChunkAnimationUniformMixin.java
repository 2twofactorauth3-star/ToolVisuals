package ru.he3hauka.toolvisuals.mixin.chunkanimator;

import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformFloat4v;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.DefaultShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.chunkanimator.*;
import ru.he3hauka.toolvisuals.client.optimization.FastShaders;

@Mixin(value=DefaultShaderInterface.class,remap=false)
public abstract class ChunkAnimationUniformMixin implements AnimatedShader {
    @Unique private GlUniformFloat4v toolvisuals$chunks,toolvisuals$timing,toolvisuals$shaders;
    @Unique private RegionUniform toolvisuals$starts;
    @Inject(method="<init>",at=@At("RETURN")) private void toolvisuals$bind(ShaderBindingContext context,ChunkShaderOptions options,CallbackInfo callback){toolvisuals$chunks=context.bindUniformOptional("u_ToolVisualsChunks",GlUniformFloat4v::new);toolvisuals$timing=context.bindUniformOptional("u_ToolVisualsTiming",GlUniformFloat4v::new);toolvisuals$shaders=context.bindUniformOptional("u_ToolVisualsShader",GlUniformFloat4v::new);toolvisuals$starts=context.bindUniformOptional("u_ToolVisualsStarts",RegionUniform::new);}
    @Inject(method="setupState",at=@At("TAIL")) private void toolvisuals$settings(CallbackInfo callback){if(toolvisuals$chunks!=null)toolvisuals$chunks.set(ChunkAnimations.distance(),ChunkAnimations.mode(),ChunkAnimations.enabled()?1:0,0);if(toolvisuals$timing!=null)toolvisuals$timing.set(RegionAnimations.millis(System.nanoTime()),1f/ChunkAnimations.durationMillis(),0,0);var shader=FastShaders.settings();if(toolvisuals$shaders!=null)toolvisuals$shaders.set(shader.flags,shader.saturation,shader.contrast,shader.warmth);}
    @Override public void toolvisuals$region(RegionAnimations animations){if(toolvisuals$starts!=null)toolvisuals$starts.set(animations.times());}
}
