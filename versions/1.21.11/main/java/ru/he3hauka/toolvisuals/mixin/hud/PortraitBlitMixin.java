package ru.he3hauka.toolvisuals.mixin.hud;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import ru.he3hauka.toolvisuals.client.hud.target.*;

@Mixin(PictureInPictureRenderer.class)
public abstract class PortraitBlitMixin {
    @ModifyArg(method="blitTexture",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/render/state/BlitRenderState;<init>(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/client/gui/render/TextureSetup;Lorg/joml/Matrix3x2f;IIIIFFFFILnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)V"),index=11)
    private int toolvisuals$fadePortrait(int color,@Local(argsOnly=true) PictureInPictureRenderState state){
        if(state instanceof GuiEntityRenderState entity&&entity.renderState() instanceof PortraitState portrait)
            return PortraitTint.color(portrait.toolvisuals$portraitOpacity(),true);
        return color;
    }
}
