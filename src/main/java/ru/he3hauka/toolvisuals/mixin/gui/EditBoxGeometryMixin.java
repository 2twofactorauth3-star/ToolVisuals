package ru.he3hauka.toolvisuals.mixin.gui;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.TextCursorUtils;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;

import ru.he3hauka.toolvisuals.client.gui.render.UiFont;

@Mixin(EditBox.class)
public abstract class EditBoxGeometryMixin {
    @Shadow @Final private Font font;
    @Shadow private int textY;
    @Shadow public abstract String getValue();
    @Unique private String toolvisuals$measuredText;
    @Unique private UiFont.TextBounds toolvisuals$bounds;

    @Unique private UiFont.TextBounds toolvisuals$bounds(){
        String value=getValue();
        if(toolvisuals$bounds==null||!value.equals(toolvisuals$measuredText)){
            toolvisuals$bounds=UiFont.textBounds(font,value);toolvisuals$measuredText=value;
        }
        return toolvisuals$bounds;
    }

    @WrapOperation(method="extractWidgetRenderState",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;textHighlight(IIIIZ)V"))
    private void toolvisuals$selection(GuiGraphicsExtractor graphics,int left,int top,int right,int bottom,boolean invert,Operation<Void> original){
        if(UiFont.owns(font)){var bounds=toolvisuals$bounds();top=bounds.selectionTop(textY);bottom=bounds.selectionBottom(textY);}
        original.call(graphics,left,top,right,bottom,invert);
    }

    @WrapOperation(method="extractWidgetRenderState",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/components/TextCursorUtils;extractInsertCursor(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIII)V"))
    private void toolvisuals$cursor(GuiGraphicsExtractor graphics,int x,int y,int color,int height,Operation<Void> original){
        if(UiFont.owns(font)){var bounds=toolvisuals$bounds();int top=bounds.selectionTop(textY);height=bounds.selectionBottom(textY)-top-1;y=top+1;}
        original.call(graphics,x,y,color,height);
    }

    @WrapOperation(method="extractWidgetRenderState",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/components/TextCursorUtils;extractAppendCursor(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIIZ)V"))
    private void toolvisuals$appendCursor(GuiGraphicsExtractor graphics,Font source,int x,int y,int color,boolean shadow,Operation<Void> original){
        if(UiFont.owns(font)){
            var bounds=toolvisuals$bounds();int top=bounds.selectionTop(textY);
            TextCursorUtils.extractInsertCursor(graphics,x-1,top+1,color,bounds.selectionBottom(textY)-top-1);
        }else original.call(graphics,source,x,y,color,shadow);
    }
}
