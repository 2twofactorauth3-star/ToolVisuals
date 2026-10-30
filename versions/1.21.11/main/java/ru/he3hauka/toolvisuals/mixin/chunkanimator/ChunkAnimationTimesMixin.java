package ru.he3hauka.toolvisuals.mixin.chunkanimator;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.he3hauka.toolvisuals.client.chunkanimator.ChunkAnimationTimes;
import ru.he3hauka.toolvisuals.client.chunkanimator.ChunkAnimations;

@Mixin(targets="net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion$DeviceResources",remap=false)
public abstract class ChunkAnimationTimesMixin {
    @Unique private final ChunkAnimationTimes toolvisuals$times=new ChunkAnimationTimes();
    @WrapMethod(method="writeMeshTimes")
    private void toolvisuals$preserve(int section,int time,Operation<Void> original) {
        if(ChunkAnimations.enabled())time=toolvisuals$times.time(0,section,time,System.nanoTime(),ChunkAnimations.durationMillis()*1_000_000L);
        else toolvisuals$times.clear();
        original.call(section,time);
    }
}
