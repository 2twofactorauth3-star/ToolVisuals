package ru.he3hauka.toolvisuals.mixin.render;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.friends.FriendRenderState;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.render.hitcolor.HitColors;

@Mixin(LivingEntityRenderer.class)
public abstract class HitColorMixin {
    @Inject(method = "getModelTint", at = @At("RETURN"), cancellable = true)
    private void toolvisuals$hitTint(LivingEntityRenderState state, CallbackInfoReturnable<Integer> cir) {
        int color=cir.getReturnValue();
        if(state instanceof FriendRenderState friend&&friend.toolvisuals$friendBodyTint()!=0) {
            int tint=UiPalette.mix(0xffffffff,friend.toolvisuals$friendBodyTint(),.75f);
            color=net.minecraft.util.ARGB.multiply(color,tint);
        }
        if (HitColors.active(ToolVisualsClient.CONFIG, state)) {
            int tint = HitColors.modelTint(ToolVisualsClient.CONFIG);
            color=net.minecraft.util.ARGB.multiply(color,tint);
        }
        if(color!=cir.getReturnValue())cir.setReturnValue(color);
    }
    @Inject(method = "getOverlayCoords", at = @At("HEAD"), cancellable = true)
    private static void toolvisuals$replaceRedOverlay(LivingEntityRenderState state, float white, CallbackInfoReturnable<Integer> cir) {
        if (HitColors.active(ToolVisualsClient.CONFIG, state))
            cir.setReturnValue(OverlayTexture.pack(OverlayTexture.u(white), OverlayTexture.v(false)));
    }
}
