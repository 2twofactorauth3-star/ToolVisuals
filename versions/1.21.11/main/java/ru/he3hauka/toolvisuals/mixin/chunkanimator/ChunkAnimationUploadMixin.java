package ru.he3hauka.toolvisuals.mixin.chunkanimator;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.he3hauka.toolvisuals.client.chunkanimator.ChunkAnimations;
import ru.he3hauka.toolvisuals.client.chunkanimator.SectionAnimation;

@Mixin(value=RenderRegionManager.class,remap=false)
public abstract class ChunkAnimationUploadMixin {
    @ModifyConstant(method="uploadResults(Lnet/caffeinemc/mods/sodium/client/gl/device/CommandList;Lnet/caffeinemc/mods/sodium/client/render/chunk/region/RenderRegion;Ljava/util/Collection;)V",constant=@Constant(doubleValue=768.0))
    private double toolvisuals$nearby(double original){return ChunkAnimations.enabled()?0:original;}
    @WrapOperation(method="uploadResults(Lnet/caffeinemc/mods/sodium/client/gl/device/CommandList;Lnet/caffeinemc/mods/sodium/client/render/chunk/region/RenderRegion;Ljava/util/Collection;)V",at=@At(value="INVOKE",target="Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSection;isBuilt()Z"))
    private boolean toolvisuals$updates(RenderSection section,Operation<Boolean> original){return original.call(section)&&!((SectionAnimation)(Object)section).toolvisuals$animateUpdate();}
}
