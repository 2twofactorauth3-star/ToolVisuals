package ru.he3hauka.toolvisuals.client.animation.ui;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.*;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Pose;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import ru.he3hauka.toolvisuals.animation.Emote;
import ru.he3hauka.toolvisuals.animation.EmoteGroup;
import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.animation.*;
import ru.he3hauka.toolvisuals.client.gui.animation.Motion;
import ru.he3hauka.toolvisuals.client.gui.layout.UiViewport;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

public final class EmoteWheelScreen extends Screen {
    private final Screen parent;
    private final Motion entrance=new Motion(0);
    private final Motion[] hover=new Motion[12];
    private List<VisualsButton> buttons=List.of();
    private float scale,cx,cy;
    private int selected=-1;
    private EmoteGroup group=EmoteGroup.SOCIAL;
    private boolean keyboard;
    private long frame,previewStart;
    public EmoteWheelScreen(Screen parent){super(tr("title"));this.parent=parent;for(int i=0;i<hover.length;i++)hover[i]=new Motion(0);}
    private static Component tr(String key,Object... args){return UiI18n.tr("toolvisuals.emotes."+key,args);}
    @Override protected void init() {
        var viewport=new UiViewport(width,height,minecraft.getWindow().getGuiScale());scale=Math.min(viewport.scale,Math.min(width/580f,height/590f));cx=width/scale/2;cy=height/scale/2+12;frame=previewStart=System.nanoTime();
        var widgets=new java.util.ArrayList<VisualsButton>();
        for(var category:EmoteGroup.values())widgets.add(addWidget(new VisualsButton(Math.round(cx-247+category.ordinal()*100),Math.round(cy-224),94,30,tr("group."+category.id),VisualsButton.Kind.CHIP,null,Component.empty(),()->category(category),()->group==category)));
        widgets.add(button(cx-134,cy+220,128,tr("back"),this::onClose));
        widgets.add(button(cx+6,cy+220,128,tr("stop"),()->{Emotes.stop(true);minecraft.gui.setScreen(null);}));
        buttons=List.copyOf(widgets);
    }
    private VisualsButton button(float x,float y,int width,Component label,Runnable action){return addWidget(new VisualsButton(Math.round(x),Math.round(y),width,30,label,VisualsButton.Kind.CHIP,null,Component.empty(),action,null));}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){g.fill(0,0,width,height,UiPaint.alpha(0x9a080812,entrance.value()));}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
        if(minecraft.player==null||!ToolVisualsClient.CONFIG.active(ru.he3hauka.toolvisuals.client.module.VisualModule.ANIMATIONS)){onClose();return;}
        long now=System.nanoTime();float elapsed=Math.clamp((now-frame)/1e9f,0,.1f);frame=now;float visible=entrance.step(1,elapsed,16);
        var palette=new UiPalette(ToolVisualsClient.CONFIG.lightTheme()?1:0);float x=mx/scale,y=my/scale;
        if(!keyboard)select(RadialMenuGeometry.index(x-cx,y-cy,88,184,hover.length));
        g.pose().pushMatrix();g.pose().scale(scale,scale);
        UiPaint.centered(g,tr("title").getString(),cx-220,cy-278,440,32,1.25f,palette.text,visible);
        UiPaint.centered(g,tr("hint").getString(),cx-230,cy-246,460,22,.75f,palette.muted,visible);
        for(int index=0;index<group.emotes().size();index++) {
            var emote=group.emotes().get(index);
            float over=hover[index].step(selected==index?1:0,elapsed,18);
            int color=UiPalette.mix(palette.panel,UiPalette.mix(palette.hover,palette.accent,.25f),over);
            UiPaint.radialSegment(g,cx,cy,88,184,index,hover.length,UiPaint.alpha(color,visible));
            double angle=-Math.PI/2+index*Math.TAU/hover.length;
            EmoteGlyph.draw(g,emote,cx+(float)Math.cos(angle)*135,cy+(float)Math.sin(angle)*135,UiPaint.alpha(UiPalette.mix(palette.muted,palette.accentText,over),visible));
            UiPaint.centered(g,""+(index+1),cx+(float)Math.cos(angle)*170-12,cy+(float)Math.sin(angle)*170-9,24,18,.65f,palette.faint,visible);
        }
        UiPaint.rounded(g,cx-82,cy-82,164,164,82,UiPaint.alpha(palette.panel,visible));
        if(selected>=0) {
            var emote=group.emotes().get(selected);UiPaint.centered(g,tr(emote.key).getString(),cx-78,cy-77,156,24,.84f,palette.text,visible);
            preview(g,emote,now,visible);
            UiPaint.centered(g,String.format(java.util.Locale.ROOT,"%.1f s",emote.duration/20f),cx-64,cy+60,128,20,.7f,palette.muted,visible);
        }else{UiPaint.icon(g,UiPaint.Icon.PLAYER,cx-18,cy-30,36,palette.accentText,visible);UiPaint.centered(g,tr("choose").getString(),cx-74,cy+15,148,22,.78f,palette.muted,visible);}
        boolean sharing=ToolVisualsClient.CONFIG.flag(ru.he3hauka.toolvisuals.client.module.VisualSetting.EMOTE_SYNC);
        UiPaint.centered(g,tr(!sharing?"private":Emotes.synchronizedServer(selected<0?null:group.emotes().get(selected))?"shared":"local").getString(),cx-236,cy+190,472,22,.75f,palette.muted,visible);
        for(var button:buttons){UiPaint.rounded(g,button.getX(),button.getY(),button.getWidth(),30,7,UiPaint.alpha(palette.panel,visible));button.frame(palette,visible,elapsed);button.extractRenderState(g,Math.round(x),Math.round(y),partial);}
        g.pose().popMatrix();
    }
    private void preview(GuiGraphicsExtractor g,Emote emote,long now,float visible) {
        if(visible<.1f)return;
        var state=(AvatarRenderState)minecraft.getEntityRenderDispatcher().getRenderer(minecraft.player).createRenderState(minecraft.player,1);
        state.nameTag=null;state.scoreText=null;state.shadowPieces.clear();state.leashStates=null;state.displayFireAnimation=false;state.isInvisible=false;state.outlineColor=0;
        state.bodyRot=180;state.yRot=0;state.xRot=0;state.walkAnimationSpeed=0;state.walkAnimationPos=0;state.hasRedOverlay=false;state.pose=Pose.STANDING;state.isCrouching=false;state.isUsingItem=false;state.swimAmount=0;state.isVisuallySwimming=false;state.isFallFlying=false;state.isPassenger=false;
        EmoteVisualState.clearPreview(state);
        ((EmoteVisualState)state).toolvisuals$emote(EmotePose.sample(emote,(now-previewStart)/50_000_000d%(emote.duration+12)));
        var first=g.pose().transformPosition(cx-72,cy-53,new Vector2f());var last=g.pose().transformPosition(cx+72,cy+60,new Vector2f());
        g.entity(state,35*scale,new Vector3f(0,.9f,0),new Quaternionf().rotationZ((float)Math.PI),new Quaternionf(),Math.round(first.x),Math.round(first.y),Math.round(last.x),Math.round(last.y));
    }
    private void category(EmoteGroup value){if(group!=value){group=value;selected=-1;keyboard=false;previewStart=System.nanoTime();for(int i=0;i<hover.length;i++)hover[i]=new Motion(0);}}
    private void select(int value){if(selected!=value){selected=value;previewStart=System.nanoTime();}}
    private MouseButtonEvent adjusted(MouseButtonEvent event){return new MouseButtonEvent(event.x()/scale,event.y()/scale,event.buttonInfo());}
    @Override public void mouseMoved(double x,double y){keyboard=false;super.mouseMoved(x/scale,y/scale);}
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean twice) {
        int index=RadialMenuGeometry.index(event.x()/scale-cx,event.y()/scale-cy,88,184,hover.length);
        if(event.button()==InputConstants.MOUSE_BUTTON_LEFT&&index>=0){Emotes.start(group.emotes().get(index));minecraft.gui.setScreen(null);return true;}
        return super.mouseClicked(adjusted(event),twice);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event){return super.mouseReleased(adjusted(event));}
    @Override public boolean keyPressed(KeyEvent event) {
        if(event.key()==InputConstants.KEY_ESCAPE||ToolVisualsClient.emoteKey.matches(event)){onClose();return true;}
        if(event.key()==InputConstants.KEY_UP||event.key()==InputConstants.KEY_DOWN||event.key()==InputConstants.KEY_TAB){var groups=EmoteGroup.values();category(groups[Math.floorMod(group.ordinal()+(event.key()==InputConstants.KEY_UP?-1:1),groups.length)]);return true;}
        if(event.key()==InputConstants.KEY_LEFT||event.key()==InputConstants.KEY_RIGHT){keyboard=true;select(Math.floorMod((selected<0?0:selected)+(event.key()==InputConstants.KEY_LEFT?-1:1),hover.length));return true;}
        if(event.key()>=InputConstants.KEY_F1&&event.key()<=InputConstants.KEY_F12){Emotes.start(group.emotes().get(event.key()-InputConstants.KEY_F1));minecraft.gui.setScreen(null);return true;}
        if(event.isConfirmation()&&selected>=0){Emotes.start(group.emotes().get(selected));minecraft.gui.setScreen(null);return true;}
        return super.keyPressed(event);
    }
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
}
