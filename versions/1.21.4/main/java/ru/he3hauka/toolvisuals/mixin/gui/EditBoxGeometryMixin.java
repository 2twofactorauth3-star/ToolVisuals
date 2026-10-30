package ru.he3hauka.toolvisuals.mixin.gui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import ru.he3hauka.toolvisuals.client.gui.render.UiFont;

@Mixin(EditBox.class)
public abstract class EditBoxGeometryMixin {
    @Shadow @Final private Font font;
    @Shadow private boolean bordered;
    @Unique private int toolvisuals$top(){var field=(EditBox)(Object)this;return field.getY()+(bordered?(field.getHeight()-8)/2:0);}
    @WrapOperation(method={"renderWidget","renderHighlight"},at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphics;fill(Lnet/minecraft/client/renderer/RenderType;IIIII)V"))
    private void toolvisuals$height(GuiGraphics graphics,RenderType type,int left,int top,int right,int bottom,int color,Operation<Void> original){if(UiFont.owns(font)){top=toolvisuals$top()-1;bottom=top+17;}original.call(graphics,type,left,top,right,bottom,color);}
    @WrapOperation(method="renderWidget",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I"))
    private int toolvisuals$cursor(GuiGraphics graphics,Font source,String text,int x,int y,int color,Operation<Integer> original){if(UiFont.owns(font)&&text.equals("_")){graphics.fill(x-1,toolvisuals$top(),x,toolvisuals$top()+15,color);return x;}return original.call(graphics,source,text,x,y,color);}
}
