package ru.he3hauka.toolvisuals.mixin.gui;

import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.gui.render.UiClip;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(method = "submitBlitFromItemAtlas",
            at = @At(value = "FIELD", target = "Lcom/mojang/renderpearl/api/textures/FilterMode;NEAREST:Lcom/mojang/renderpearl/api/textures/FilterMode;"))
    private com.mojang.renderpearl.api.textures.FilterMode toolvisuals$smoothHudSampler(
            com.mojang.renderpearl.api.textures.FilterMode original,
            @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) net.minecraft.client.renderer.state.gui.GuiItemRenderState item) {
        return ((ru.he3hauka.toolvisuals.client.hud.items.SmoothHudItems.State)(Object)item).toolvisuals$smoothItem()
                ? com.mojang.renderpearl.api.textures.FilterMode.LINEAR : original;
    }
    @org.spongepowered.asm.mixin.injection.ModifyArg(method = "prepareItemElements", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/render/GuiRenderer;prepareItemAtlas(Ljava/util/Set;I)Lnet/minecraft/client/gui/render/GuiItemAtlas;"), index = 1)
    private int toolvisuals$itemResolution(int original) { return Math.max(32, original); }
    @Inject(method = "executeDraw", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$skipEmptyClip(@Coerce Object draw, RenderPass pass, CallbackInfo ci) {
        var clip = ((GuiDrawAccessor)draw).toolvisuals$scissorArea();
        if (clip == null) return;
        var window = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState;
         
        if (UiClip.empty(clip.left(), clip.top(), clip.right(), clip.bottom(), window.guiScale, window.width, window.height)) ci.cancel();
    }
}
