package ru.he3hauka.toolvisuals.mixin.chunkanimator;

import java.util.Collection;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.BuilderTaskOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegionManager;
import org.spongepowered.asm.mixin.Mixin;
import ru.he3hauka.toolvisuals.client.chunkanimator.AnimatedRegion;

@Mixin(value=RenderRegionManager.class,remap=false)
public abstract class ChunkAnimationUploadMixin {
    @WrapMethod(method="uploadResults(Lnet/caffeinemc/mods/sodium/client/gl/device/CommandList;Ljava/util/Collection;)V")
    private void toolvisuals$uploaded(CommandList commands,Collection<BuilderTaskOutput> results,Operation<Void> original){original.call(commands,results);for(var result:results)if(result instanceof ChunkBuildOutput){var section=result.render;((AnimatedRegion)(Object)section.getRegion()).toolvisuals$animations().uploaded(section.getSectionIndex());}}
}
