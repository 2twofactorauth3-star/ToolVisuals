package ru.he3hauka.toolvisuals.mixin.chunkanimator;

import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformFloat;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformFloat4v;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.DefaultShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.chunkanimator.ChunkAnimations;
import ru.he3hauka.toolvisuals.client.optimization.FastShaders;

@Mixin(value=DefaultShaderInterface.class,remap=false)
public abstract class ChunkAnimationUniformMixin {
    @Shadow @Final private GlUniformFloat uniformFadePeriod;
    @Unique private GlUniformFloat4v toolvisuals$chunks,toolvisuals$shaders;
    @Inject(method="<init>",at=@At("RETURN"))
    private void toolvisuals$bind(ShaderBindingContext context,ChunkShaderOptions options,CallbackInfo callback) {
        toolvisuals$chunks=context.bindUniformOptional("u_ToolVisualsChunks",GlUniformFloat4v::new);
        toolvisuals$shaders=context.bindUniformOptional("u_ToolVisualsShader",GlUniformFloat4v::new);
    }
    @Inject(method="setupState",at=@At("TAIL"))
    private void toolvisuals$uniforms(CallbackInfo callback) {
        boolean enabled=ChunkAnimations.enabled();
        if(enabled)uniformFadePeriod.setFloat(1f/ChunkAnimations.durationMillis());
        if(toolvisuals$chunks!=null)toolvisuals$chunks.set(ChunkAnimations.distance(),ChunkAnimations.mode(),enabled?1:0,0);
        var settings=FastShaders.settings();
        if(toolvisuals$shaders!=null)toolvisuals$shaders.set(settings.flags,settings.saturation,settings.contrast,settings.warmth);
    }
}
