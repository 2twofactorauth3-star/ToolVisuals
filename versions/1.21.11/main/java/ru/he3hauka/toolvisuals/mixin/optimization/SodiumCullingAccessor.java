package ru.he3hauka.toolvisuals.mixin.optimization;

import net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value=AbstractBlockRenderContext.class,remap=false)
public interface SodiumCullingAccessor {
    @Accessor("enableCulling") boolean toolvisuals$culling();
}
