package ru.he3hauka.toolvisuals.mixin.gui;

import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.he3hauka.toolvisuals.client.gui.render.SmoothSampling;

@Mixin(AbstractTexture.class)
public abstract class TextureSamplingMixin implements SmoothSampling {
    @Unique private boolean toolvisuals$smoothSampling;

    @Override
    public void toolvisuals$enableSmoothSampling() {
        toolvisuals$smoothSampling = true;
    }

    @ModifyVariable(method = "setFilter(ZZ)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private boolean toolvisuals$preserveSmoothSampling(boolean requested) {
        return toolvisuals$smoothSampling || requested;
    }
}
