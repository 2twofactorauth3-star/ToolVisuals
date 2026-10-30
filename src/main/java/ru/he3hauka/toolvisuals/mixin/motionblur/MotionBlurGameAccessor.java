package ru.he3hauka.toolvisuals.mixin.motionblur;

import java.util.List;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface MotionBlurGameAccessor {
    @Accessor("appliedPostEffects") List<PostChain> toolvisuals$motionEffects();
}
