package ru.he3hauka.toolvisuals.relay.paper.cape;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import ru.he3hauka.toolvisuals.animation.network.*;
import ru.he3hauka.toolvisuals.network.RevisionOutbox;
import ru.he3hauka.toolvisuals.cosmetic.cape.*;

public final class CapeRelay {
    public static final class Endpoint {
        private final PacketBudget inputBudget;
        public final RelayMailbox<CapeWire.Request> incoming=new RelayMailbox<>(1);
        public final RevisionOutbox<CapeWire.State> outgoing=new RevisionOutbox<>(256,state->new RevisionOutbox.Identity(state.actor(),state.epoch(),state.revision()),state->state.design()==null);
        private CapeWire.Ack ack;
        public Endpoint(Endpoint previous){inputBudget=previous==null?new PacketBudget(1,1,System.nanoTime()):previous.inputBudget;}
        public synchronized void acknowledge(CapeWire.Ack value){if(ack==null||value.requestId()>=ack.requestId())ack=value;}
        public synchronized CapeWire.Ack acknowledgement(){return ack;}
        public synchronized void sent(CapeWire.Ack value){if(ack==value)ack=null;}
        public synchronized boolean pending(){return ack!=null||outgoing.next()!=null;}
        public synchronized void close(){incoming.close();outgoing.close();ack=null;}
    }
    private final CapeSessions sessions;
    private final UUID server;
    private final ru.he3hauka.toolvisuals.network.fragment.PacketFragments.Assembler<UUID> uploads=new ru.he3hauka.toolvisuals.network.fragment.PacketFragments.Assembler<>(64);
    public record Fragment(boolean accepted,byte[] bytes){}
    private final PacketBudget inputBudget=new PacketBudget(16,64,System.nanoTime());
    private final PacketBudget animationBudget=new PacketBudget(4,8,System.nanoTime());
    private final PacketBudget outputBytes=new PacketBudget(512*1024,256*1024,System.nanoTime());
    private final Map<UUID,PacketBudget> recipientBytes=new HashMap<>();
    private final Map<UUID,Long> animationAfter=new HashMap<>();
    private final RelayLimits sends=new RelayLimits(System.nanoTime(),32,128,8,16);
    public CapeRelay(UUID server){this.server=server;sessions=new CapeSessions(server,RelayLimits.MAX_PLAYERS);}
    public synchronized boolean accept(Endpoint endpoint,long now){
        if(!endpoint.inputBudget.available(now)||!inputBudget.available(now))return false;
        endpoint.inputBudget.allow(now);inputBudget.allow(now);return true;
    }
    public synchronized Fragment fragment(Endpoint endpoint,UUID actor,long epoch,CapeTransferWire.RequestPart part,long now){
        if(part.epoch()!=epoch)return new Fragment(false,null);
        if(!uploads.contains(actor,part.epoch(),part.requestId(),now)&&!accept(endpoint,now))return new Fragment(false,null);
        return new Fragment(true,uploads.accept(actor,part.epoch(),part.requestId(),part.fragment(),now));
    }
    public CapeSessions.Update update(UUID actor,long epoch,CapeWire.Request request){return update(actor,epoch,request,System.nanoTime());}
    public CapeSessions.Update update(UUID actor,long epoch,CapeWire.Request request,long now){
        var design=CapeAnimationPolicy.limit(request.design());
        var normalized=design==request.design()?request:new CapeWire.Request(request.requestId(),request.epoch(),request.receive(),design);
        synchronized(this){
            var old=sessions.state(actor);
            boolean changing=request.epoch()==epoch&&design!=null&&design.frameCount>1&&(old==null||old.epoch()!=epoch||!design.equals(old.design()));
            if(changing&&(animationAfter.containsKey(actor)&&now-animationAfter.get(actor)<0||!animationBudget.available(now)))
                return new CapeSessions.Update(new CapeWire.Ack(old==null?server:old.server(),request.requestId(),epoch,old==null?0:old.revision(),false),null,false);
            var result=sessions.update(actor,epoch,normalized);
            if(changing&&result.ack().accepted()){animationBudget.allow(now);animationAfter.put(actor,now+CapeAnimationPolicy.UPDATE_INTERVAL);}
            return result;
        }
    }
    public CapeWire.State state(UUID actor){return sessions.state(actor);}
    public synchronized CapeWire.State remove(UUID actor,long epoch){uploads.remove(actor,epoch);var removed=sessions.remove(actor,epoch);if(removed!=null){sends.remove(actor);recipientBytes.remove(actor);animationAfter.remove(actor);}return removed;}
    public boolean receives(UUID actor,long epoch){return sessions.receives(actor,epoch);}
    public synchronized boolean send(UUID recipient,int bytes,long now){
        var budget=recipientBytes.get(recipient);
        if(budget==null){if(recipientBytes.size()>=RelayLimits.MAX_PLAYERS)return false;budget=new PacketBudget(64*1024,32*1024,now);recipientBytes.put(recipient,budget);}
        if(!outputBytes.available(now,bytes)||!budget.available(now,bytes)){sends.idle(recipient);return false;}
        if(sends.delivery(recipient,now)!=RelayLimits.Delivery.SEND)return false;
        outputBytes.allow(now,bytes);budget.allow(now,bytes);return true;
    }
    public static int deliveryBytes(CapeWire.State state){
        int bytes=49+(state.design()==null?0:2+state.design().frameCount*CapeDesign.BYTES);
        return bytes+Math.ceilDiv(bytes,ru.he3hauka.toolvisuals.network.fragment.PacketFragments.DATA_BYTES)*(48+ru.he3hauka.toolvisuals.network.fragment.PacketFragments.HEADER_BYTES);
    }
    public void idle(UUID recipient){sends.idle(recipient);}
}
