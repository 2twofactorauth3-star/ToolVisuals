package ru.he3hauka.toolvisuals.mixin.motionblur;

import java.util.Map;
import net.minecraft.client.renderer.PostPass;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PostPass.class)
public interface MotionBlurPassAccessor {
    @Accessor("customUniforms") Map<String, GpuBuffer> toolvisuals$motionUniforms();
}
