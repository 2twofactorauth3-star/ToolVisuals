package ru.he3hauka.toolvisuals.client.social.profile;

import java.io.IOException;
import java.util.UUID;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.cape.*;
import ru.he3hauka.toolvisuals.client.cosmetic.accessory.AccessoryClient;
import ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.*;
import ru.he3hauka.toolvisuals.client.cosmetic.preview.PlayerPreview;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.nameprotect.NameProtect;
import ru.he3hauka.toolvisuals.client.social.*;
import ru.he3hauka.toolvisuals.client.social.ui.PairedEmoteScreen;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.ANIMATIONS;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.EMOTE_SYNC;

public final class PlayerCardScreen extends VisualEditorScreen {
    private final UUID actor;
    private final String name;
    private final CapeTexture cape=new CapeTexture(true);
    private final long opened=System.nanoTime();
    private Object connection;
    private RemotePlayer preview;
    private boolean turning;
    private float yaw=180;
    private String status="";
    private VisualsButton friend,pair;

    public PlayerCardScreen(Screen parent,UUID actor,String name){super(parent,text("title"));this.actor=java.util.Objects.requireNonNull(actor);this.name=java.util.Objects.requireNonNull(name);}
    private static Component text(String key,Object... args){return UiI18n.tr("toolvisuals.profile."+key,args);}
    @Override protected void init(){
        frame(660,438);if(connection==null)connection=minecraft.getConnection();
        friend=button(336,218,298,32,text("add_friend"),UiPaint.Icon.STAR,()->{try{if(ToolVisualsClient.FRIENDS.get(name)==null)ToolVisualsClient.FRIENDS.add(name);else ToolVisualsClient.FRIENDS.remove(name);status="";}catch(IOException error){status=text("io_error").getString();}},()->ToolVisualsClient.FRIENDS.get(name)!=null);
        pair=button(336,260,298,32,text("invite"),UiPaint.Icon.PLAYER,()->{if(canInvite())minecraft.gui.setScreen(new PairedEmoteScreen(actor));},null);
        button(486,388,148,28,UiI18n.tr("toolvisuals.ui.back"),UiPaint.Icon.BACK,this::onClose,null);
    }
    private boolean canInvite(){return connection==minecraft.getConnection()&&minecraft.level!=null&&ToolVisualsClient.CONFIG.active(ANIMATIONS)
            &&ToolVisualsClient.CONFIG.flag(EMOTE_SYNC)&&SocialClient.available()&&PairTargets.available(minecraft,minecraft.level.getPlayerByUUID(actor));}
    @Override protected void paint(GuiGraphicsExtractor g,int mx,int my){
        var info=connection==minecraft.getConnection()&&minecraft.getConnection()!=null?minecraft.getConnection().getPlayerInfo(actor):null;
        boolean online=info!=null&&minecraft.level!=null;
        friend.setMessage(text(ToolVisualsClient.FRIENDS.get(name)==null?"add_friend":"remove_friend"));
        friend.visible=ToolVisualsClient.CONFIG.available(ru.he3hauka.toolvisuals.client.connection.policy.ClientFeature.FRIENDS);
        pair.visible=ToolVisualsClient.CONFIG.available(ANIMATIONS);
        friend.active=friend.visible&&(minecraft.player==null||!actor.equals(minecraft.player.getUUID()));pair.active=online&&canInvite();
        UiPaint.rounded(g,px+24,py+66,288,306,9,palette.field);
        UiPaint.baseline(g,UiPaint.fit(g,Component.literal(NameProtect.text(name)),298,1.2f),px+336,UiPaint.textBaseline(g,py+72,36,1.2f),1.2f,palette.text,1);
        line(g,text(online?"online":"offline"),336,118,.8f,online?palette.accentText:palette.muted);
        if(info!=null)line(g,text("ping",info.getLatency()),336,148,.8f,palette.muted);
        line(g,text("client"),336,178,.8f,palette.accentText);
        if(pair.visible)line(g,text(pair.active?"invite_hint":"nearby_hint"),336,306,.71f,palette.muted);
        line(g,text("preview_hint"),336,334,.69f,palette.muted);
        if(online){
            if(preview==null||preview.level()!=minecraft.level)preview=new RemotePlayer(minecraft.level,info.getProfile());
            var state=PlayerPreview.prepare(preview,false,false);state.bodyRot=yaw;
            double seconds=(System.nanoTime()-opened)/1e9;
            var look=ToolVisualsClient.CONFIG.available(ru.he3hauka.toolvisuals.client.module.VisualModule.COSMETICS)?AccessoryClient.look(actor):null;if(look!=null&&!look.empty())((AccessoryVisualState)state).toolvisuals$accessories(new AccessoryFrame(look,seconds,0,false,true,false));
            var design=ToolVisualsClient.CONFIG.available(ru.he3hauka.toolvisuals.client.module.VisualModule.CAPES)?CapeClient.design(actor):null;if(design!=null)cape.apply(state,design);
            PlayerPreview.draw(g,state,px+30,py+72,276,292,110,1.05f);
        }
        if(!status.isEmpty())line(g,Component.literal(status),24,390,.72f,palette.muted);
    }
    private void line(GuiGraphicsExtractor g,Component message,int x,int y,float size,int color){UiPaint.baseline(g,UiPaint.fit(g,message,x>=336?298:440,size),px+x,UiPaint.textBaseline(g,py+y,24,size),size,color,1);}
    @Override public boolean mouseClicked(MouseButtonEvent raw,boolean twice){var event=adjusted(raw);if(event.button()==InputConstants.MOUSE_BUTTON_LEFT&&event.x()>=px+24&&event.x()<px+312&&event.y()>=py+66&&event.y()<py+372){turning=true;return true;}return super.mouseClicked(raw,twice);}
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){if(turning){yaw+=(float)(dx/scale*.7);return true;}return super.mouseDragged(event,dx,dy);}
    @Override public boolean mouseReleased(MouseButtonEvent event){turning=false;return super.mouseReleased(event);}
    @Override public void removed(){cape.close();super.removed();}
}
