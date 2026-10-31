package ru.he3hauka.toolvisuals.relay.paper.cape;

import java.util.UUID;
import ru.he3hauka.toolvisuals.animation.network.*;
import ru.he3hauka.toolvisuals.network.RevisionOutbox;
import ru.he3hauka.toolvisuals.cosmetic.cape.*;

public final class CapeRelay {
    public static final class Endpoint {
        private final PacketBudget inputBudget;
        public final RelayMailbox<CapeWire.Request> incoming=new RelayMailbox<>(1);
        public final RevisionOutbox<CapeWire.State> outgoing=new RevisionOutbox<>(256,state->new RevisionOutbox.Identity(state.actor(),state.epoch(),state.revision()),state->state.design()==null);
        private CapeWire.Ack ack;
        public Endpoint(Endpoint previous){inputBudget=previous==null?new PacketBudget(2,1,System.nanoTime()):previous.inputBudget;}
        public synchronized void acknowledge(CapeWire.Ack value){if(ack==null||value.requestId()>=ack.requestId())ack=value;}
        public synchronized CapeWire.Ack acknowledgement(){return ack;}
        public synchronized void sent(CapeWire.Ack value){if(ack==value)ack=null;}
        public synchronized boolean pending(){return ack!=null||outgoing.next()!=null;}
        public synchronized void close(){incoming.close();outgoing.close();ack=null;}
    }
    private final CapeSessions sessions;
    private final ru.he3hauka.toolvisuals.network.fragment.PacketFragments.Assembler<UUID> uploads=new ru.he3hauka.toolvisuals.network.fragment.PacketFragments.Assembler<>(64);
    public record Fragment(boolean accepted,byte[] bytes){}
    private final PacketBudget inputBudget=new PacketBudget(32,128,System.nanoTime());
    private final RelayLimits sends=new RelayLimits(System.nanoTime(),64,256,16,32);
    public CapeRelay(UUID server){sessions=new CapeSessions(server,RelayLimits.MAX_PLAYERS);}
    public synchronized boolean accept(Endpoint endpoint,long now){
        if(!endpoint.inputBudget.available(now)||!inputBudget.available(now))return false;
        endpoint.inputBudget.allow(now);inputBudget.allow(now);return true;
    }
    public synchronized Fragment fragment(Endpoint endpoint,UUID actor,long epoch,CapeTransferWire.RequestPart part,long now){
        if(part.epoch()!=epoch)return new Fragment(false,null);
        if(!uploads.contains(actor,part.epoch(),part.requestId(),now)&&!accept(endpoint,now))return new Fragment(false,null);
        return new Fragment(true,uploads.accept(actor,part.epoch(),part.requestId(),part.fragment(),now));
    }
    public CapeSessions.Update update(UUID actor,long epoch,CapeWire.Request request){return sessions.update(actor,epoch,request);}
    public CapeWire.State state(UUID actor){return sessions.state(actor);}
    public CapeWire.State remove(UUID actor,long epoch){uploads.remove(actor,epoch);sends.remove(actor);return sessions.remove(actor,epoch);}
    public boolean receives(UUID actor,long epoch){return sessions.receives(actor,epoch);}
    public boolean send(UUID recipient,long now){return sends.delivery(recipient,now)==RelayLimits.Delivery.SEND;}
    public void idle(UUID recipient){sends.idle(recipient);}
}
