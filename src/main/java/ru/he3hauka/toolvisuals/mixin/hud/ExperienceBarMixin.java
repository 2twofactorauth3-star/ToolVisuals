package ru.he3hauka.toolvisuals.mixin.hud;

import net.minecraft.client.gui.contextualbar.ExperienceBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.render.filter.NoRender;

import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.*;

@Mixin(ExperienceBar.class)
public abstract class ExperienceBarMixin {
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractBackground(CallbackInfo ci) {
        if (NoRender.hides(EXPERIENCE_BAR)) ci.cancel();
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractRenderState(CallbackInfo ci) {
        if (NoRender.hides(EXPERIENCE_BAR)) ci.cancel();
    }
}

