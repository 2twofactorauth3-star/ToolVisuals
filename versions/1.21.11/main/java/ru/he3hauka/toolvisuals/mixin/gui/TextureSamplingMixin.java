package ru.he3hauka.toolvisuals.mixin.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.gui.render.SmoothSampling;

@Mixin(AbstractTexture.class)
public abstract class TextureSamplingMixin implements SmoothSampling {
    @Unique private boolean toolvisuals$smooth;
    @Override public void toolvisuals$enableSmoothSampling(){toolvisuals$smooth=true;}
    @Inject(method="getSampler",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$linearSampler(CallbackInfoReturnable<GpuSampler> callback){
        if(toolvisuals$smooth)callback.setReturnValue(RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
    }
}
