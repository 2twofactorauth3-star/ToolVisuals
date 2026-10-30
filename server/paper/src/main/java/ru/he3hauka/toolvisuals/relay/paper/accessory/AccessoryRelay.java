package ru.he3hauka.toolvisuals.relay.paper.accessory;

import java.util.UUID;
import ru.he3hauka.toolvisuals.animation.network.*;
import ru.he3hauka.toolvisuals.network.RevisionOutbox;
import ru.he3hauka.toolvisuals.cosmetic.accessory.*;

public final class AccessoryRelay {
    public static final class Endpoint {
        private final PacketBudget inputBudget;
        public final RelayMailbox<AccessoryWire.Request> incoming=new RelayMailbox<>(1);
        public final RevisionOutbox<AccessoryWire.State> outgoing=new RevisionOutbox<>(256,state->new RevisionOutbox.Identity(state.actor(),state.epoch(),state.revision()),state->state.design()==null);
        private AccessoryWire.Ack ack;
        public Endpoint(Endpoint previous){inputBudget=previous==null?new PacketBudget(2,1,System.nanoTime()):previous.inputBudget;}
        public synchronized void acknowledge(AccessoryWire.Ack value){if(ack==null||value.requestId()>=ack.requestId())ack=value;}
        public synchronized AccessoryWire.Ack acknowledgement(){return ack;}
        public synchronized void sent(AccessoryWire.Ack value){if(ack==value)ack=null;}
        public synchronized boolean pending(){return ack!=null||outgoing.next()!=null;}
        public synchronized void close(){incoming.close();outgoing.close();ack=null;}
    }
    private final AccessorySessions sessions;
    private final PacketBudget inputBudget=new PacketBudget(16,64,System.nanoTime());
    private final RelayLimits sends=new RelayLimits(System.nanoTime(),64,256,16,32);
    public AccessoryRelay(UUID server){sessions=new AccessorySessions(server,RelayLimits.MAX_PLAYERS);}
    public synchronized boolean accept(Endpoint endpoint,long now){
        if(!endpoint.inputBudget.available(now)||!inputBudget.available(now))return false;
        endpoint.inputBudget.allow(now);inputBudget.allow(now);return true;
    }
    public AccessorySessions.Update update(UUID actor,long epoch,AccessoryWire.Request request){return sessions.update(actor,epoch,request);}
    public AccessoryWire.State state(UUID actor){return sessions.state(actor);}
    public AccessoryWire.State remove(UUID actor,long epoch){sends.remove(actor);return sessions.remove(actor,epoch);}
    public boolean receives(UUID actor,long epoch){return sessions.receives(actor,epoch);}
    public boolean send(UUID recipient,long now){return sends.delivery(recipient,now)==RelayLimits.Delivery.SEND;}
    public void idle(UUID recipient){sends.idle(recipient);}
}
