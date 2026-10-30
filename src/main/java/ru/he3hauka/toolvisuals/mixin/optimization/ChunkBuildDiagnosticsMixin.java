package ru.he3hauka.toolvisuals.mixin.optimization;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import org.spongepowered.asm.mixin.Mixin;

import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;

@Mixin(value=ChunkBuilderMeshingTask.class,remap=false)
public abstract class ChunkBuildDiagnosticsMixin {
    @WrapMethod(method="execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;")
    private ChunkBuildOutput toolvisuals$build(ChunkBuildContext context,CancellationToken cancellation,Operation<ChunkBuildOutput> original){
        long started=Diagnostics.start(Diagnostics.Section.CHUNK_BUILD);
        try{return original.call(context,cancellation);}finally{Diagnostics.end(Diagnostics.Section.CHUNK_BUILD,started);}
    }
}
