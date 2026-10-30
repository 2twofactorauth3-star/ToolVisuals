package ru.he3hauka.toolvisuals.mixin.chunkanimator;

import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.chunkanimator.AnimatedRegion;

@Mixin(value=RenderSection.class,remap=false)
public abstract class ChunkAnimationSectionMixin {
    @Inject(method="delete",at=@At("HEAD")) private void toolvisuals$forget(CallbackInfo callback){var section=(RenderSection)(Object)this;((AnimatedRegion)(Object)section.getRegion()).toolvisuals$animations().forget(section.getSectionIndex());}
}
