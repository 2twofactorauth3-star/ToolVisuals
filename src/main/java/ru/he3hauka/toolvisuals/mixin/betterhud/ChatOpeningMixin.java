package ru.he3hauka.toolvisuals.mixin.betterhud;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.hud.animation.BetterHud;

@Mixin(ChatScreen.class)
public abstract class ChatOpeningMixin {
    @Inject(method="init",at=@At("TAIL"))
    private void toolvisuals$chatOpened(CallbackInfo callback){BetterHud.chatOpened(this);}
    @WrapMethod(method="extractRenderState")
    private void toolvisuals$opening(GuiGraphicsExtractor graphics,int x,int y,float partial,Operation<Void> original){
        float offset=BetterHud.chatOpeningOffset(),previous=BetterHud.chatRenderOffset();
        graphics.pose().pushMatrix();graphics.pose().translate(0,offset);BetterHud.chatRenderOffset(offset);
        try{original.call(graphics,x,Math.round(y-offset),partial);}
        finally{BetterHud.chatRenderOffset(previous);graphics.pose().popMatrix();}
    }
    @WrapMethod(method="mouseClicked")
    private boolean toolvisuals$openingClick(MouseButtonEvent event,boolean twice,Operation<Boolean> original){
        float previous=BetterHud.chatInputOffset();BetterHud.chatInputOffset(BetterHud.chatOpeningOffset());
        try{return original.call(BetterHud.animatedChatInput(event),twice);}
        finally{BetterHud.chatInputOffset(previous);}
    }
    @WrapMethod(method="mouseScrolled")
    private boolean toolvisuals$openingScroll(double x,double y,double horizontal,double vertical,Operation<Boolean> original){
        return original.call(x,y-BetterHud.chatOpeningOffset(),horizontal,vertical);
    }
}
