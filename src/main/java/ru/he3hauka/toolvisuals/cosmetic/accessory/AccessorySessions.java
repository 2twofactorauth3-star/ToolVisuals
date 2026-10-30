package ru.he3hauka.toolvisuals.cosmetic.accessory;

import java.util.UUID;

import ru.he3hauka.toolvisuals.cosmetic.sync.CosmeticSessions;

public final class AccessorySessions extends CosmeticSessions<AccessoryLook,AccessoryWire.State> {
    public record Update(AccessoryWire.Ack ack,AccessoryWire.State changed,boolean resync){}
    public AccessorySessions(UUID server,int maximum){super(server,maximum,AccessoryWire.State::new);}
    public Update update(UUID actor,long epoch,AccessoryWire.Request request){
        var result=update(actor,epoch,request.requestId(),request.epoch(),request.receive(),request.design());
        return new Update(new AccessoryWire.Ack(server,result.requestId(),result.epoch(),result.revision(),result.accepted()),result.changed(),result.resync());
    }
}
