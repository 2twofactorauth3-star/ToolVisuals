package ru.he3hauka.toolvisuals.mixin.optimization;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.BuilderTaskOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import ru.he3hauka.toolvisuals.client.optimization.frame.FrameWork;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.OPT_CHUNK_BUDGET;

@Mixin(value=RenderSectionManager.class,remap=false)
public abstract class ChunkBudgetMixin {
    @Unique private int toolvisuals$remaining;
    @WrapMethod(method="collectChunkBuildResults")
    private ArrayList<BuilderTaskOutput> toolvisuals$budget(Operation<ArrayList<BuilderTaskOutput>> original){toolvisuals$remaining=Optimizations.enabled(OPT_CHUNK_BUDGET)?Math.clamp((int)(FrameWork.chunks(2_000_000L)/75_000L),4,64):Integer.MAX_VALUE;try{return original.call();}finally{toolvisuals$remaining=Integer.MAX_VALUE;}}
    @WrapOperation(method="collectChunkBuildResults",at=@At(value="INVOKE",target="Ljava/util/concurrent/ConcurrentLinkedDeque;poll()Ljava/lang/Object;"))
    private Object toolvisuals$boundedUploads(ConcurrentLinkedDeque<?> queue,Operation<Object> original){return toolvisuals$remaining-->0?original.call(queue):null;}
}
