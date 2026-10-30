package ru.he3hauka.toolvisuals.client.session;

import net.minecraft.client.Minecraft;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

public final class SessionStats {
    private static final SessionCounter COUNTER=new SessionCounter();
    private SessionStats(){}
    public static void tick(Minecraft client){
        if(ReplayScope.active())return;long now=System.nanoTime();COUNTER.scope(client.getConnection(),now);
        if(client.player==null||client.level==null)COUNTER.observe(null,null,false,0,0);
        else COUNTER.observe(client.level,client.player,client.player.isDeadOrDying(),client.player.getX(),client.player.getZ());
    }
    public static void broken(){if(!ReplayScope.active())COUNTER.broken();}
    public static void death(){if(ReplayScope.active())return;var client=Minecraft.getInstance();COUNTER.scope(client.getConnection(),System.nanoTime());COUNTER.death(client.player);}
    public static void reset(){COUNTER.reset(System.nanoTime());}
    public static SessionCounter.Snapshot snapshot(){return COUNTER.snapshot(System.nanoTime());}
}
