package ru.he3hauka.toolvisuals.mixin.motionblur;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface MotionBlurGameAccessor {
    @Accessor("resourcePool") CrossFrameResourcePool toolvisuals$motionPool();
}
