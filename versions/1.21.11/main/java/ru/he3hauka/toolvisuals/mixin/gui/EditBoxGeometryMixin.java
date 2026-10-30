package ru.he3hauka.toolvisuals.mixin.gui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import ru.he3hauka.toolvisuals.client.gui.render.UiFont;

@Mixin(EditBox.class)
public abstract class EditBoxGeometryMixin {
    @Shadow @Final private Font font;
    @Shadow private int textY;
    @Shadow public abstract String getValue();
    @Unique private String toolvisuals$text;
    @Unique private UiFont.TextBounds toolvisuals$bounds;
    @Unique private UiFont.TextBounds toolvisuals$bounds(){String next=getValue();if(toolvisuals$bounds==null||!next.equals(toolvisuals$text)){toolvisuals$text=next;toolvisuals$bounds=UiFont.textBounds(font,next);}return toolvisuals$bounds;}
    @WrapOperation(method="renderWidget",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphics;textHighlight(IIIIZ)V"))
    private void toolvisuals$selection(GuiGraphics graphics,int left,int top,int right,int bottom,boolean invert,Operation<Void> original){
        if(UiFont.owns(font)){var bounds=toolvisuals$bounds();top=bounds.selectionTop(textY);bottom=bounds.selectionBottom(textY);}original.call(graphics,left,top,right,bottom,invert);
    }
    @WrapOperation(method="renderWidget",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"))
    private void toolvisuals$cursor(GuiGraphics graphics,int left,int top,int right,int bottom,int color,Operation<Void> original){
        if(UiFont.owns(font)){var bounds=toolvisuals$bounds();top=bounds.selectionTop(textY)+1;bottom=bounds.selectionBottom(textY);}original.call(graphics,left,top,right,bottom,color);
    }
    @WrapOperation(method="renderWidget",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V"))
    private void toolvisuals$append(GuiGraphics graphics,Font source,String text,int x,int y,int color,boolean shadow,Operation<Void> original){
        if(text.equals("_")&&UiFont.owns(font)){var bounds=toolvisuals$bounds();graphics.fill(x-1,bounds.selectionTop(textY)+1,x,bounds.selectionBottom(textY),color);}
        else original.call(graphics,source,text,x,y,color,shadow);
    }
}
