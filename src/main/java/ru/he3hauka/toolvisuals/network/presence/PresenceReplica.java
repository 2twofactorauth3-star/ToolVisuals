package ru.he3hauka.toolvisuals.network.presence;

import java.util.*;

public final class PresenceReplica {
    private static final long ASSEMBLY_NANOS=300_000_000_000L,LIFETIME_NANOS=600_000_000_000L;
    private UUID server;
    private long epoch,revision,highestRevision,pendingRevision,started,confirmed;
    private int total,received;
    private List<UUID>[] pending;
    private Set<UUID> players=Set.of();
    private boolean ready;

    public void scope(UUID server,long epoch){
        if(Objects.equals(this.server,server)&&this.epoch==epoch)return;
        this.server=server;this.epoch=epoch;revision=highestRevision=confirmed=0;ready=false;players=Set.of();discard();
    }

    public Set<UUID> players(){return players;}
    public boolean ready(){return ready;}

    public boolean accept(PresenceWire.Page page,long now){
        if(server==null||epoch<=0||!server.equals(page.server())||epoch!=page.epoch()||page.revision()<=revision)return false;
        if(pending!=null&&now-started>ASSEMBLY_NANOS)discard();
        if(page.revision()<highestRevision)return false;
        highestRevision=page.revision();
        if(pending==null||page.revision()>pendingRevision)begin(page,now);
        if(page.total()!=total)return false;
        var old=pending[page.index()];
        if(old!=null){if(!old.equals(page.players()))discard();return false;}
        pending[page.index()]=page.players();received++;
        if(received!=pending.length)return false;
        var next=new HashSet<UUID>(total);
        for(var part:pending)for(var player:part)if(!next.add(player)){discard();return false;}
        if(next.size()!=total){discard();return false;}
        if(!players.equals(next))players=Set.copyOf(next);
        revision=pendingRevision;confirmed=now;ready=true;discard();return true;
    }

    @SuppressWarnings("unchecked")
    private void begin(PresenceWire.Page page,long now){
        pending=(List<UUID>[])new List<?>[PresenceWire.pages(page.total())];
        pendingRevision=page.revision();total=page.total();received=0;started=now;
    }

    private void discard(){pending=null;pendingRevision=0;total=received=0;}
    public void expire(long now){
        if(pending!=null&&now-started>ASSEMBLY_NANOS)discard();
        if(ready&&now-confirmed>LIFETIME_NANOS){ready=false;players=Set.of();}
    }
}
