package ru.he3hauka.toolvisuals.cosmetic.cape;

import java.util.UUID;

import ru.he3hauka.toolvisuals.cosmetic.sync.CosmeticSessions;

public final class CapeSessions extends CosmeticSessions<CapeDesign,CapeWire.State> {
    public record Update(CapeWire.Ack ack,CapeWire.State changed,boolean resync){}
    public CapeSessions(UUID server,int maximum){super(server,maximum,CapeWire.State::new);}
    public Update update(UUID actor,long epoch,CapeWire.Request request){
        var result=update(actor,epoch,request.requestId(),request.epoch(),request.receive(),request.design());
        return new Update(new CapeWire.Ack(server,result.requestId(),result.epoch(),result.revision(),result.accepted()),result.changed(),result.resync());
    }
}
