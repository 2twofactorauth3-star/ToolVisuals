package ru.he3hauka.toolvisuals.mixin.itemhighlights;

import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.items.highlight.ItemHighlights;

@Mixin(GuiItemRenderState.class)
public abstract class ItemGlowStateMixin implements ItemHighlights.GuiState {
    @Unique private ItemHighlights.GuiStyle toolvisuals$glow;
    @Inject(method = "<init>", at = @At("RETURN"))
    private void toolvisuals$captureGlow(CallbackInfo callback) { toolvisuals$glow = ItemHighlights.extracting(); }
    @Override public ItemHighlights.GuiStyle toolvisuals$itemGlow() { return toolvisuals$glow; }
}
