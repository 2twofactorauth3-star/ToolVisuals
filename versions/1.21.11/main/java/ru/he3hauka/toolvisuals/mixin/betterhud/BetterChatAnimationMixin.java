package ru.he3hauka.toolvisuals.mixin.betterhud;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.hud.animation.BetterHud;

@Mixin(ChatComponent.class)
public abstract class BetterChatAnimationMixin {
    @Inject(method="addMessageToDisplayQueue",at=@At("TAIL")) private void toolvisuals$arrival(CallbackInfo callback){BetterHud.messageAdded();}
    @WrapMethod(method="render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;IIIZZ)V")
    private void toolvisuals$animation(GuiGraphics graphics,Font font,int ticks,int x,int y,boolean focused,boolean hovered,Operation<Void> original) {
        graphics.pose().pushMatrix();graphics.pose().translate(0,BetterHud.chatOffset());
        try{original.call(graphics,font,ticks,x,y,focused,hovered);}finally{graphics.pose().popMatrix();}
    }
}
