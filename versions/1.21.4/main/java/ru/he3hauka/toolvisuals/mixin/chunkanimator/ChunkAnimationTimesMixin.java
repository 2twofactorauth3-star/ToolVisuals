package ru.he3hauka.toolvisuals.mixin.chunkanimator;

import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.he3hauka.toolvisuals.client.chunkanimator.AnimatedRegion;
import ru.he3hauka.toolvisuals.client.chunkanimator.RegionAnimations;

@Mixin(value=RenderRegion.class,remap=false)
public abstract class ChunkAnimationTimesMixin implements AnimatedRegion {
    @Unique private final RegionAnimations toolvisuals$animations=new RegionAnimations();
    @Override public RegionAnimations toolvisuals$animations(){return toolvisuals$animations;}
}
