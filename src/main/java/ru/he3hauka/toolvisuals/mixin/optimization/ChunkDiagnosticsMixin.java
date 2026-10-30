package ru.he3hauka.toolvisuals.mixin.optimization;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.caffeinemc.mods.sodium.client.render.chunk.*;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.BuilderTaskOutput;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import org.spongepowered.asm.mixin.Mixin;

import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;

import static ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.Section.*;

@Mixin(value=RenderSectionManager.class,remap=false)
public abstract class ChunkDiagnosticsMixin {
    @WrapMethod(method="updateChunks")
    private void toolvisuals$schedule(Viewport viewport,boolean immediate,Operation<Void> original){
        long started=Diagnostics.start(CHUNK_SCHEDULE);
        try{original.call(viewport,immediate);}finally{Diagnostics.end(CHUNK_SCHEDULE,started);}
    }
    @WrapMethod(method="processChunkBuilds")
    private void toolvisuals$upload(Viewport viewport,UniformBufferManager uniforms,Operation<Void> original){
        long started=Diagnostics.start(CHUNK_UPLOAD),workStarted=System.nanoTime();
        try{original.call(viewport,uniforms);}finally{
            ru.he3hauka.toolvisuals.client.optimization.frame.FrameWork.consumed(System.nanoTime()-workStarted,false);
            Diagnostics.end(CHUNK_UPLOAD,started);
        }
    }
    @WrapMethod(method="collectChunkBuildResults")
    private java.util.ArrayList<BuilderTaskOutput> toolvisuals$completed(Operation<java.util.ArrayList<BuilderTaskOutput>> original){
        var result=original.call();
        Diagnostics.count(Diagnostics.Counter.CHUNK_COMPLETED,result.size());
        return result;
    }
}
