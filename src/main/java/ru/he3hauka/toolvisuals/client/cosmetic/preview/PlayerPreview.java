package ru.he3hauka.toolvisuals.client.cosmetic.preview;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import ru.he3hauka.toolvisuals.client.animation.EmoteVisualState;
import ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.AccessoryVisualState;

public final class PlayerPreview {
    private static final ThreadLocal<Boolean> PREPARING=ThreadLocal.withInitial(()->false);
    private PlayerPreview(){}
    public static boolean preparing(){return PREPARING.get();}
    public static AvatarRenderState prepare(AbstractClientPlayer player,boolean armor,boolean accessories){
        var client=Minecraft.getInstance();AvatarRenderState state;boolean previous=PREPARING.get();PREPARING.set(true);
        try{state=(AvatarRenderState)client.getEntityRenderDispatcher().getRenderer(player).createRenderState(player,1);}finally{if(previous)PREPARING.set(true);else PREPARING.remove();}
        EmoteVisualState.clearPreview(state);if(!accessories)AccessoryVisualState.clear(state);
        if(accessories){var look=ru.he3hauka.toolvisuals.client.cosmetic.accessory.AccessoryClient.look(player.getUUID());if(look!=null&&!look.empty())((AccessoryVisualState)state).toolvisuals$accessories(new ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.AccessoryFrame(look,System.nanoTime()/1e9,0,false,true,false));}
        state.nameTag=null;state.scoreText=null;state.shadowPieces.clear();state.leashStates=null;state.displayFireAnimation=false;
        state.isInvisible=false;state.isInvisibleToPlayer=false;state.outlineColor=0;state.hasRedOverlay=false;state.deathTime=0;
        state.yRot=0;state.xRot=0;state.pose=Pose.STANDING;state.isSpectator=false;state.isCrouching=false;state.isUsingItem=false;
        state.swimAmount=0;state.isVisuallySwimming=false;state.isFallFlying=false;state.isPassenger=false;state.walkAnimationPos=0;state.walkAnimationSpeed=0;
        if(!armor){state.chestEquipment=ItemStack.EMPTY;state.headEquipment=ItemStack.EMPTY;state.legsEquipment=ItemStack.EMPTY;state.feetEquipment=ItemStack.EMPTY;}
        return state;
    }
    public static void draw(GuiGraphicsExtractor g,AvatarRenderState state,float x,float y,float width,float height,float size,float center){
        var first=g.pose().transformPosition(x,y,new Vector2f());var last=g.pose().transformPosition(x+width,y+height,new Vector2f());
        float scale=(float)Math.hypot(g.pose().m00(),g.pose().m01());
        g.entity(state,size*scale,new Vector3f(0,center,0),new Quaternionf().rotationZ((float)Math.PI),new Quaternionf(),Math.round(first.x),Math.round(first.y),Math.round(last.x),Math.round(last.y));
    }
}
