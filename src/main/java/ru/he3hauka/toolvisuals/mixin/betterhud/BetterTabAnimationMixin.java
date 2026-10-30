package ru.he3hauka.toolvisuals.mixin.betterhud;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.*;
import org.spongepowered.asm.mixin.Mixin;

import ru.he3hauka.toolvisuals.client.hud.animation.BetterHud;

@Mixin(PlayerTabOverlay.class)
public abstract class BetterTabAnimationMixin {
    @WrapMethod(method="extractRenderState")
    private void toolvisuals$tabAnimation(GuiGraphicsExtractor g,int width,Scoreboard scoreboard,Objective objective,Operation<Void> original) {
        float progress=BetterHud.tabProgress();boolean clipped=progress<.9999f;
        if(clipped) {
             
            g.pose().pushMatrix();g.pose().identity();
            g.enableScissor(0,0,g.guiWidth(),Math.round(g.guiHeight()*progress*progress));g.pose().popMatrix();
        }
        g.pose().pushMatrix();g.pose().translate(0,BetterHud.tabOffset());
        try { original.call(g,width,scoreboard,objective); } finally { g.pose().popMatrix();if(clipped)g.disableScissor(); }
    }
}
