package ru.he3hauka.toolvisuals.client.animation.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.*;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.animation.EffectTime;
import ru.he3hauka.toolvisuals.client.animation.pose.PlayerPoseState;

import static ru.he3hauka.toolvisuals.client.animation.movement.MovementPoses.Activity.*;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.EXTRA_ANIMATIONS;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class ExtraAnimations {
    private static final PoseTransitions TRANSITIONS=new PoseTransitions();
    private static Object connection,world,player;
    private ExtraAnimations(){}
    public static void initialize(){ru.he3hauka.toolvisuals.client.replay.integration.ReplayLifecycle.unload((entity,level)->TRANSITIONS.remove(entity.getUUID()));}
    public static void tick(Minecraft client){
        scope(client);
        if(client.level!=null)TRANSITIONS.expire(client.level.getGameTime());
    }
    private static void scope(Minecraft client){
        if(connection!=client.getConnection()||world!=client.level||player!=client.player||!ToolVisualsClient.CONFIG.active(EXTRA_ANIMATIONS)){TRANSITIONS.clear();connection=client.getConnection();world=client.level;player=client.player;}
    }
    public static void extract(Entity entity,AvatarRenderState state,float partial,boolean emote){
        var visual=(PlayerPoseState)state;visual.toolvisuals$actionPose(null);
        if(ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active())return;
        var client=Minecraft.getInstance();var config=ToolVisualsClient.CONFIG;
        scope(client);
        if(emote||client.player==null||client.level==null||entity.level()!=client.level||!config.active(EXTRA_ANIMATIONS)||!(entity instanceof LivingEntity actor)
                ||!actor.isAlive()||state.isSpectator||state.isInvisible||!config.flag(entity==client.player?EXTRA_SELF:EXTRA_OTHERS))return;
        var activity=NONE;boolean left=false;float rowingRight=0,rowingLeft=0;
        double clock=EffectTime.at(client.level.getGameTime(),partial),phase=clock*.12;
        if(actor.isUsingItem()){
            left=actor.getMainArm()==HumanoidArm.LEFT;if(actor.getUsedItemHand()==net.minecraft.world.InteractionHand.OFF_HAND)left=!left;
            if(config.flag(EXTRA_USE_ITEMS))activity=switch(actor.getUseItem().getUseAnimation()){case EAT->EAT;case DRINK->DRINK;case BLOCK->BLOCK;default->NONE;};
            phase=actor.getTicksUsingItem(partial)*.9;
        }else if(config.flag(EXTRA_CLIMB)&&actor.onClimbable()){activity=CLIMB;phase=actor.getY()*4;}
        else if(config.flag(EXTRA_CRAWL)&&actor.isVisuallyCrawling()&&!actor.isInWater()){activity=CRAWL;phase=state.walkAnimationPos*1.6;}
        else if(config.flag(EXTRA_RIDING)&&actor.getVehicle() instanceof AbstractBoat boat){activity=ROW;rowingRight=boat.getRowingTime(0,partial);rowingLeft=boat.getRowingTime(1,partial);}
        else if(config.flag(EXTRA_RIDING)&&actor.getVehicle() instanceof AbstractHorse)activity=RIDE;
        else if(config.flag(EXTRA_MAPS)){
            var right=state.rightHandItemStack;var leftStack=state.leftHandItemStack;
            boolean rightMap=right.is(Items.FILLED_MAP),leftMap=leftStack.is(Items.FILLED_MAP);
            if(rightMap&&leftStack.isEmpty()||leftMap&&right.isEmpty())activity=MAP;
            else if(rightMap||leftMap||inspect(right)||inspect(leftStack)){activity=INSPECT;left=leftMap||inspect(leftStack)&&!inspect(right);}
        }
        if(activity==BLOCK){state.bodyRot+=state.yRot*.6f;state.yRot*=.4f;}
        var input=new MovementPoses.Input(activity,left,phase,state.xRot,rowingRight,rowingLeft);
        visual.toolvisuals$actionPose(TRANSITIONS.frame(entity.getUUID(),client.player.getUUID(),input,clock,config.value(EXTRA_TRANSITION)).strength((float)config.value(EXTRA_STRENGTH)));
    }
    private static boolean inspect(ItemStack stack){return stack.is(Items.CLOCK)||stack.is(Items.COMPASS)||stack.is(Items.RECOVERY_COMPASS);}
}
