package ru.he3hauka.toolvisuals.relay.paper.presence;

import java.util.*;
import ru.he3hauka.toolvisuals.animation.network.*;
import ru.he3hauka.toolvisuals.network.presence.*;

public final class PresenceRelay {
    public static final class Endpoint {
        public final RelayMailbox<PresenceWire.Subscription> incoming=new RelayMailbox<>(1);
        public final PresenceOutbox outgoing;
        public final java.util.concurrent.atomic.AtomicBoolean requested=new java.util.concurrent.atomic.AtomicBoolean();
        public final java.util.concurrent.atomic.AtomicBoolean scheduled=new java.util.concurrent.atomic.AtomicBoolean();
        public volatile long lastRequest;
        private final PacketBudget input;
        public Map<UUID,Long> observed;
        public long nextVisibilityCheck;
        public Endpoint(UUID server,long epoch,Endpoint previous){
            outgoing=new PresenceOutbox(server,epoch);
            input=previous==null?new PacketBudget(2,1,System.nanoTime()):previous.input;
        }
        public void close(){requested.set(false);incoming.close();outgoing.clear();observed=null;}
    }
    public final PresenceDirectory directory=new PresenceDirectory();
    private final PacketBudget input=new PacketBudget(32,64,System.nanoTime());
    private final RelayLimits output=new RelayLimits(System.nanoTime(),32,256,4,8);

    public synchronized boolean accept(Endpoint peer,long now){
        if(!peer.input.available(now)||!input.available(now))return false;
        peer.input.allow(now);input.allow(now);return true;
    }
    public boolean send(UUID player,long now){return output.delivery(player,now)==RelayLimits.Delivery.SEND;}
    public void idle(UUID player){output.idle(player);}
    public void remove(UUID player,long epoch){directory.advertise(player,epoch,false);output.remove(player);}
}
