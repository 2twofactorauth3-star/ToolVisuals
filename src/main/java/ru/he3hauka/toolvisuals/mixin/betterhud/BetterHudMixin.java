package ru.he3hauka.toolvisuals.mixin.betterhud;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import ru.he3hauka.toolvisuals.client.hud.animation.BetterHud;

@Mixin(Hud.class)
public abstract class BetterHudMixin {
    @ModifyExpressionValue(method="extractTabList",at=@At(value="INVOKE",target="Lnet/minecraft/client/KeyMapping;isDown()Z"))
    private boolean toolvisuals$smoothTabVisibility(boolean held) { return BetterHud.tabVisible(held); }
    @ModifyArgs(method="extractItemHotbar",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void toolvisuals$smoothHotbar(Args args) {
        if(((Identifier)args.get(1)).getPath().equals("hud/hotbar_selection"))args.set(2,BetterHud.hotbarX(args.get(2)));
    }
}
