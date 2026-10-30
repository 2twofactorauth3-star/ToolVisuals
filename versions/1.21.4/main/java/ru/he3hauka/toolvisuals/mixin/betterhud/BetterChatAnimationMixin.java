package ru.he3hauka.toolvisuals.mixin.betterhud;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.hud.animation.BetterHud;

@Mixin(ChatComponent.class)
public abstract class BetterChatAnimationMixin {
    @Inject(method="addMessageToDisplayQueue",at=@At("TAIL"))
    private void toolvisuals$chatArrival(CallbackInfo ci) { BetterHud.messageAdded(); }
    @WrapMethod(method="render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V")
    private void toolvisuals$chatAnimation(GuiGraphicsExtractor g,int ticks,int x,int y,boolean focused,Operation<Void> original) {
        g.pose().pushMatrix();g.pose().translate(0,BetterHud.chatOffset());
        try { original.call(g,ticks,x,y,focused); } finally { g.pose().popMatrix(); }
    }
}
