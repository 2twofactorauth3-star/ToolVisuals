package ru.he3hauka.toolvisuals.mixin.chunkanimator;

import net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.render.viewport.CameraTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.chunkanimator.AnimatedShader;
import ru.he3hauka.toolvisuals.client.chunkanimator.AnimatedRegion;

@Mixin(value=DefaultChunkRenderer.class,remap=false)
public abstract class ChunkAnimationRegionMixin {
    @Inject(method="setModelMatrixUniforms",at=@At("HEAD")) private static void toolvisuals$region(ChunkShaderInterface shader,RenderRegion region,CameraTransform camera,CallbackInfo callback){if(ru.he3hauka.toolvisuals.client.chunkanimator.ChunkAnimations.enabled())((AnimatedShader)shader).toolvisuals$region(((AnimatedRegion)(Object)region).toolvisuals$animations());}
}
