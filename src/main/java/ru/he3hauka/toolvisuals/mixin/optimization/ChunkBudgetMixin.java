package ru.he3hauka.toolvisuals.mixin.optimization;

import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;
import ru.he3hauka.toolvisuals.client.optimization.terrain.ChunkBudget;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.OPT_CHUNK_BUDGET;

@Mixin(value=RenderSectionManager.class,remap=false)
public abstract class ChunkBudgetMixin {
    @Shadow private long averageFrameDuration;
    @Shadow private int thisFrameBlockingTasks,nextFrameBlockingTasks,deferredTasks;

    @ModifyArg(method="updateChunks",at=@At(value="INVOKE",target="Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/estimation/LimitedResourceBudget;<init>(JJ)V"),index=0)
    private long toolvisuals$uploadBudget(long duration){return Optimizations.enabled(OPT_CHUNK_BUDGET)?ru.he3hauka.toolvisuals.client.optimization.frame.FrameWork.chunks(ChunkBudget.upload(duration,averageFrameDuration)):duration;}

    @ModifyArg(method="updateChunks",at=@At(value="INVOKE",target="Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/executor/ChunkJobCollector;<init>(JLjava/util/function/Consumer;)V"),index=0)
    private long toolvisuals$backgroundBudget(long duration){return Optimizations.enabled(OPT_CHUNK_BUDGET)?ChunkBudget.background(duration,averageFrameDuration,Runtime.getRuntime().availableProcessors()):duration;}

    @Inject(method="updateChunks",at=@At("RETURN"))
    private void toolvisuals$countJobs(CallbackInfo callback){
        Diagnostics.count(Diagnostics.Counter.CHUNK_URGENT,thisFrameBlockingTasks+nextFrameBlockingTasks);
        Diagnostics.count(Diagnostics.Counter.CHUNK_BACKGROUND,deferredTasks);
    }
}
