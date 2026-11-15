package ru.he3hauka.toolvisuals.mixin.itemhighlights;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.items.highlight.render.HandItemGlow;

@Mixin(ItemStackRenderState.class)
public abstract class ItemGlowHandStateMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void toolvisuals$collect(PoseStack pose, MultiBufferSource buffers, int light, int overlay, CallbackInfo callback) { HandItemGlow.collect((ItemStackRenderState)(Object)this, pose, light, overlay); }
}
