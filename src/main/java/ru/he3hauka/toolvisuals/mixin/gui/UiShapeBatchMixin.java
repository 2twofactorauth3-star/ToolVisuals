package ru.he3hauka.toolvisuals.mixin.gui;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.gui.render.UiShapeBatch;

@Mixin(GuiRenderState.class)
public abstract class UiShapeBatchMixin {
    @Inject(method = "addGuiElement", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$batchShape(GuiElementRenderState state, CallbackInfo ci) {
        if (UiShapeBatch.capture((GuiRenderState)(Object)this, state)) ci.cancel();
    }
}
