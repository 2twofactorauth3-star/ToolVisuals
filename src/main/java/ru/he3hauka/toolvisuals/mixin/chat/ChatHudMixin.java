package ru.he3hauka.toolvisuals.mixin.chat;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.animation.BetterHud;
import ru.he3hauka.toolvisuals.client.hud.layout.*;
import ru.he3hauka.toolvisuals.client.hud.layout.resize.HudResize;
import ru.he3hauka.toolvisuals.client.hud.layout.resize.HudScale;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;

@Mixin(ChatScreen.class)
public abstract class ChatHudMixin extends Screen implements ChatHudAccess {
    @Unique private final HudDragging toolvisuals$dragging=new HudDragging();
    @Unique private HudElement toolvisuals$selectedHud;
    @Unique private final ru.he3hauka.toolvisuals.client.chat.ChatResizer toolvisuals$resizer=new ru.he3hauka.toolvisuals.client.chat.ChatResizer();
    @Unique private ru.he3hauka.toolvisuals.client.hand.ui.HandEditorOverlay toolvisuals$hands;
    @Unique private ru.he3hauka.toolvisuals.client.hand.ui.HandEditorOverlay toolvisuals$hands(){
        if(toolvisuals$hands==null)toolvisuals$hands=new ru.he3hauka.toolvisuals.client.hand.ui.HandEditorOverlay(this,false,ru.he3hauka.toolvisuals.client.hand.HandSide.RIGHT);
        return toolvisuals$hands;
    }
    @Override public ru.he3hauka.toolvisuals.client.chat.ChatResizer toolvisuals$chatResizer(){return toolvisuals$resizer;}
    protected ChatHudMixin(){super(Component.empty());}
    @Override public HudDragging toolvisuals$hudDragging(){return toolvisuals$dragging;}
    @Unique private boolean toolvisuals$eligible(HudElement element){return minecraft.player!=null&&!minecraft.gui.hud.isHidden()&&!NoRender.hides(RenderOption.ALL_HUD)&&HudDragging.enabled(element,ToolVisualsClient.CONFIG);}
    @Inject(method="init",at=@At("HEAD"))
    private void toolvisuals$resetHudBounds(CallbackInfo callback){if(toolvisuals$hands!=null)toolvisuals$hands.finish();toolvisuals$saveHud();toolvisuals$selectedHud=null;HudPlacement.beginFrame();}
    @Inject(method="mouseClicked",at=@At(value="INVOKE",target="Lnet/minecraft/client/input/MouseButtonEvent;button()I",ordinal=0),cancellable=true)
    private void toolvisuals$grabHud(MouseButtonEvent event,boolean twice,CallbackInfoReturnable<Boolean> callback){
        var uiEvent=event;
        if(event.button()==InputConstants.MOUSE_BUTTON_LEFT&&toolvisuals$resizer.begin(event.x(),event.y())){toolvisuals$selectedHud=null;callback.setReturnValue(true);return;}
        event=BetterHud.originalChatInput(event);
        if(minecraft.player==null)return;
        for(var child:children())if(child.isMouseOver(uiEvent.x(),uiEvent.y())){toolvisuals$selectedHud=null;return;}
        if(twice&&event.button()==InputConstants.MOUSE_BUTTON_LEFT&&HudDragging.pick(HudPlacement.bounds(),this::toolvisuals$eligible,event.x(),event.y())==HudElement.CLIENTS&&ru.he3hauka.toolvisuals.client.social.presence.ui.ClientListHud.openAt(event.x(),event.y())){callback.setReturnValue(true);return;}
        if(event.button()==InputConstants.MOUSE_BUTTON_LEFT){
            if(toolvisuals$eligible(HudElement.MUSIC)&&HudDragging.pick(HudPlacement.bounds(),this::toolvisuals$eligible,event.x(),event.y())==HudElement.MUSIC
                    &&ru.he3hauka.toolvisuals.client.music.ui.MusicHud.click(event.x(),event.y(),twice)){toolvisuals$selectedHud=null;callback.setReturnValue(true);return;}
            if(toolvisuals$dragging.begin(HudPlacement.bounds(),this::toolvisuals$eligible,event.x(),event.y())){toolvisuals$selectedHud=toolvisuals$dragging.selected();callback.setReturnValue(true);return;}
            toolvisuals$selectedHud=null;
        }
        if(toolvisuals$hands().click(event,twice))callback.setReturnValue(true);
    }
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){
        if(event.button()==InputConstants.MOUSE_BUTTON_LEFT&&toolvisuals$resizer.dragging()){var adjusted=BetterHud.animatedChatInput(event);return toolvisuals$resizer.move(adjusted.x(),adjusted.y());}
        if(event.button()==InputConstants.MOUSE_BUTTON_LEFT&&toolvisuals$dragging.move(event.x(),event.y(),width,height,this::toolvisuals$eligible))return true;
        if(toolvisuals$hands!=null&&toolvisuals$hands.move(event))return true;
        return super.mouseDragged(BetterHud.animatedChatInput(event),dx,dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event){
        if(event.button()==InputConstants.MOUSE_BUTTON_LEFT&&toolvisuals$resizer.dragging()){toolvisuals$resizer.finish();return true;}
        if(event.button()==InputConstants.MOUSE_BUTTON_LEFT&&toolvisuals$dragging.dragging()){toolvisuals$saveHud();return true;}
        if(toolvisuals$hands!=null&&toolvisuals$hands.release(event))return true;return super.mouseReleased(BetterHud.animatedChatInput(event));
    }
    @Inject(method="mouseScrolled",at=@At(value="INVOKE",target="Lnet/minecraft/client/Minecraft;hasShiftDown()Z"),cancellable=true)
    private void toolvisuals$scaleHand(double x,double y,double horizontal,double vertical,CallbackInfoReturnable<Boolean> callback) {
        if(toolvisuals$resizeHud(x,y,vertical)){callback.setReturnValue(true);return;}
        if(toolvisuals$hands().scroll(x,y+BetterHud.chatOpeningOffset(),vertical))callback.setReturnValue(true);
    }
    @Unique private boolean toolvisuals$resizeHud(double x,double y,double vertical){
        if(toolvisuals$selectedHud==null||vertical==0||!Double.isFinite(vertical)||toolvisuals$resizer.dragging())return false;
        var config=ToolVisualsClient.CONFIG;var bounds=HudPlacement.bounds();
        if(!toolvisuals$eligible(toolvisuals$selectedHud)||!bounds.containsKey(toolvisuals$selectedHud)){toolvisuals$selectedHud=null;return false;}
        if(!HudScale.enabled(toolvisuals$selectedHud,config))return false;
        for(var child:children())if(child.isMouseOver(x,y))return false;
        double worldY=y+BetterHud.chatOpeningOffset();var box=bounds.get(toolvisuals$selectedHud);
        if(toolvisuals$dragging.dragging())box=HudResize.positioned(box,toolvisuals$dragging.position(toolvisuals$selectedHud,config.hudPositions().get(toolvisuals$selectedHud)),width,height);
        if(!box.contains(x,worldY))return false;
        var change=HudResize.change(toolvisuals$selectedHud,box,config.hudScale(toolvisuals$selectedHud),vertical,width,height,18);
        if(change!=null){
            config.resizeHud(change.element(),change.scale(),change.position());
            HudPlacement.updateBounds(change.element(),change.bounds());toolvisuals$dragging.resized(change.element(),change.bounds(),x,worldY);
        }
        return true;
    }
    @Unique private void toolvisuals$saveHud(){
        var changes=toolvisuals$dragging.finish();if(changes.isEmpty())return;var positions=new java.util.EnumMap<HudElement,HudPosition>(HudElement.class);positions.putAll(ToolVisualsClient.CONFIG.hudPositions());
        changes.forEach((element,position)->{if(toolvisuals$eligible(element))positions.put(element,position);});ToolVisualsClient.CONFIG.applyHudLayout(positions,false);
    }
    @Inject(method="removed",at=@At("HEAD"))
    private void toolvisuals$finishHud(CallbackInfo callback){if(toolvisuals$hands!=null)toolvisuals$hands.finish();toolvisuals$resizer.finish();toolvisuals$saveHud();}
    @Inject(method="extractRenderState",at=@At("TAIL"))
    private void toolvisuals$highlightHud(GuiGraphicsExtractor graphics,int mx,int my,float partial,CallbackInfo callback){
        toolvisuals$resizer.paint(graphics,mx,my);
        graphics.pose().pushMatrix();graphics.pose().translate(0,-BetterHud.chatRenderOffset());
        try{toolvisuals$hands().paint(graphics,mx,my+Math.round(BetterHud.chatRenderOffset()),partial);}finally{graphics.pose().popMatrix();}
        for(var child:children())if(child.isMouseOver(mx,my))return;
        my+=Math.round(BetterHud.chatRenderOffset());
        var bounds=HudPlacement.bounds();
        if(toolvisuals$selectedHud!=null&&(!toolvisuals$eligible(toolvisuals$selectedHud)||!bounds.containsKey(toolvisuals$selectedHud)))toolvisuals$selectedHud=null;
        var element=toolvisuals$selectedHud==null?HudDragging.pick(bounds,this::toolvisuals$eligible,mx,my):toolvisuals$selectedHud;if(element==null)return;
        var box=bounds.get(element);var palette=new UiPalette(ToolVisualsClient.CONFIG.lightTheme()?1:0);
        graphics.pose().pushMatrix();graphics.pose().translate(0,-BetterHud.chatRenderOffset());
        try{
            graphics.outline(Math.round(box.x)-1,Math.round(box.y)-1,Math.round(box.width)+2,Math.round(box.height)+2,palette.accent);
            if(element==toolvisuals$selectedHud&&HudScale.enabled(element,ToolVisualsClient.CONFIG)){
                String text=ru.he3hauka.toolvisuals.client.localization.UiI18n.tr("toolvisuals.hud.wheel_hint",Math.round(ToolVisualsClient.CONFIG.hudScale(element)*100)).getString();
                float labelY=box.y>=18?box.y-18:Math.min(height-18,box.y+box.height+2);
                ru.he3hauka.toolvisuals.client.gui.render.UiPaint.text(graphics,text,box.x,labelY,.65f,palette.accentText,1);
            }
        }
        finally{graphics.pose().popMatrix();}
    }
}
