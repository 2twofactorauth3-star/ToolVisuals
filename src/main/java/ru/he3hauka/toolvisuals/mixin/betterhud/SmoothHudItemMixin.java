package ru.he3hauka.toolvisuals.mixin.betterhud;

import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.hud.items.SmoothHudItems;

@Mixin(GuiItemRenderState.class)
public abstract class SmoothHudItemMixin implements SmoothHudItems.State {
    @Unique private boolean toolvisuals$smooth;
    @Inject(method = "<init>", at = @At("RETURN"))
    private void toolvisuals$markSmooth(CallbackInfo ci) { toolvisuals$smooth = SmoothHudItems.extracting(); }
    @Override public boolean toolvisuals$smoothItem() { return toolvisuals$smooth; }
}
