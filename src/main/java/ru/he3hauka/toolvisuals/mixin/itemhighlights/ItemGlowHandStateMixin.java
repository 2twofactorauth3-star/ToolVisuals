package ru.he3hauka.toolvisuals.mixin.itemhighlights;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.items.highlight.render.HandItemGlow;

@Mixin(ItemStackRenderState.class)
public abstract class ItemGlowHandStateMixin {
    @Inject(method = "submit", at = @At("HEAD"))
    private void toolvisuals$collect(PoseStack pose, SubmitNodeCollector nodes, int light, int overlay, int outline, CallbackInfo callback) { HandItemGlow.collect((ItemStackRenderState)(Object)this, pose, light, overlay); }
}
