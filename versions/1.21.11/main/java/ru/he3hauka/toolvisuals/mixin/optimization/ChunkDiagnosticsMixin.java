package ru.he3hauka.toolvisuals.mixin.optimization;

import java.util.ArrayList;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.BuilderTaskOutput;
import org.spongepowered.asm.mixin.Mixin;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;
import ru.he3hauka.toolvisuals.client.optimization.frame.FrameWork;
import static ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.Section.*;

@Mixin(value=RenderSectionManager.class,remap=false)
public abstract class ChunkDiagnosticsMixin {
    @WrapMethod(method="updateChunks")
    private void toolvisuals$schedule(boolean immediate,Operation<Void> original){long began=Diagnostics.start(CHUNK_SCHEDULE);try{original.call(immediate);}finally{Diagnostics.end(CHUNK_SCHEDULE,began);}}
    @WrapMethod(method="uploadChunks")
    private void toolvisuals$upload(Operation<Void> original){long began=Diagnostics.start(CHUNK_UPLOAD),work=System.nanoTime();try{original.call();}finally{FrameWork.consumed(System.nanoTime()-work,false);Diagnostics.end(CHUNK_UPLOAD,began);}}
    @WrapMethod(method="collectChunkBuildResults")
    private ArrayList<BuilderTaskOutput> toolvisuals$completed(Operation<ArrayList<BuilderTaskOutput>> original){var result=original.call();Diagnostics.count(Diagnostics.Counter.CHUNK_COMPLETED,result.size());return result;}
}
