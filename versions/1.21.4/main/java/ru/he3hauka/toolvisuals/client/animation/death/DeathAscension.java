package ru.he3hauka.toolvisuals.client.animation.death;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.phys.AABB;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.animation.EffectTime;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.DEATH_ASCENSION;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class DeathAscension {
    private record Appearance(PlayerSkin skin,net.minecraft.world.entity.EntityType<?> type,float yaw,int light){}
    private static final AscensionTimeline<Appearance> TIMELINE=new AscensionTimeline<>();
    private static Object connection,world;
    private DeathAscension(){}
    private static void scope(Minecraft client){
        if(connection!=client.getConnection()||world!=client.level||!ToolVisualsClient.CONFIG.active(DEATH_ASCENSION)){TIMELINE.clear();connection=client.getConnection();world=client.level;}
    }
    public static void event(ClientboundEntityEventPacket packet){
        var client=Minecraft.getInstance();scope(client);
        if(packet.getEventId()==3&&client.level!=null&&packet.getEntity(client.level) instanceof Player player)capture(player);
    }
    public static void preview(){var client=Minecraft.getInstance();if(client.player!=null)capture(client.player);}
    private static void capture(Player player){
        var client=Minecraft.getInstance();scope(client);var config=ToolVisualsClient.CONFIG;
        if(client.player==null||client.level==null||player.level()!=client.level||!config.active(DEATH_ASCENSION)||player.isInvisible()||player.isSpectator()||NoRender.hidesEntity(player)
                ||!config.flag(player==client.player?DEATH_SELF:DEATH_OTHERS)||player.distanceToSqr(client.player)>Math.pow(config.value(DEATH_DISTANCE),2))return;
        var state=(AvatarRenderState)client.getEntityRenderDispatcher().getRenderer(player).createRenderState(player,1);
        if(state.skin==null)return;
        TIMELINE.start(player.getUUID(),player.getId(),new Appearance(state.skin,player.getType(),state.bodyRot,client.getEntityRenderDispatcher().getRenderer(player).getPackedLightCoords(player,1)),player.getX(),player.getY(),player.getZ(),client.level.getGameTime()/20d,config.value(DEATH_DURATION),config.value(DEATH_HEIGHT));
    }
    public static void extract(LevelRenderState level){
        var client=Minecraft.getInstance();scope(client);var config=ToolVisualsClient.CONFIG;
        if(client.player==null||client.level==null||!config.active(DEATH_ASCENSION)||NoRender.hides(ru.he3hauka.toolvisuals.client.render.filter.RenderOption.PLAYERS))return;
        double now=EffectTime.at(level.gameTime,level.worldPartialTicks)/20,range=config.value(DEATH_DISTANCE);var souls=TIMELINE.active(now);
        if(config.flag(DEATH_HIDE_BODY))level.entityRenderStates.removeIf(state->state instanceof AvatarRenderState avatar&&avatar.deathTime>0&&TIMELINE.containsEntity(avatar.id));
        for(var soul:souls){
            boolean own=soul.actor().equals(client.player.getUUID());if(!config.flag(own?DEATH_SELF:DEATH_OTHERS))continue;
            var source=client.level.getPlayerByUUID(soul.actor());if(source!=null&&(source.isInvisible()||NoRender.hidesEntity(source)))continue;
            var frame=AscensionTimeline.frame(soul,now);double y=soul.y()+frame.rise();var camera=level.cameraRenderState;
            double distance=camera.pos.distanceToSqr(soul.x(),y,soul.z());if(distance>range*range)continue;
            if(camera.cullFrustum!=null&&!camera.cullFrustum.isVisible(new AABB(soul.x()-.5,y-.1,soul.z()-.5,soul.x()+.5,y+2,soul.z()+.5)))continue;
            var appearance=soul.appearance();var state=new SoulRenderState(appearance.skin,appearance.type,appearance.yaw,soul,frame,config.color(DEATH_TINT),appearance.light);state.distanceToCameraSq=distance;
            level.entityRenderStates.add(state);
        }
    }
}
