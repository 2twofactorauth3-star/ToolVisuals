package ru.he3hauka.toolvisuals.mixin.itemhighlights;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface ItemGlowGameAccessor {
    @Accessor("featureRenderDispatcher") FeatureRenderDispatcher toolvisuals$itemFeatures();
    @Accessor("resourcePool") CrossFrameResourcePool toolvisuals$itemPool();
}
