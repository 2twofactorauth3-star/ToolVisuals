package ru.he3hauka.toolvisuals.client.utility.packs;

import java.util.*;
import java.util.function.Consumer;

public final class PackSpoofSession {
    public enum Consent {PENDING,ALLOWED,DECLINED}
    public enum Stage {ACCEPTED,DOWNLOADED,APPLIED,DECLINED,DISCARDED}
    public record Event(UUID id,Stage stage){}
    private final Set<UUID> pending=new LinkedHashSet<>();
    public void request(UUID id,Consent consent,Consumer<Event> feedback) {
        pending.remove(id);
        switch(consent) {
            case ALLOWED->complete(id,true,feedback);
            case DECLINED->complete(id,false,feedback);
            case PENDING->{pending.add(id);if(pending.size()>128){var oldest=pending.iterator().next();pending.remove(oldest);feedback.accept(new Event(oldest,Stage.DISCARDED));}}
        }
    }
    public void decide(boolean accepted,Consumer<Event> feedback){var ids=List.copyOf(pending);pending.clear();ids.forEach(id->complete(id,accepted,feedback));}
    private static void complete(UUID id,boolean accepted,Consumer<Event> feedback) {
        if(accepted){feedback.accept(new Event(id,Stage.ACCEPTED));feedback.accept(new Event(id,Stage.DOWNLOADED));feedback.accept(new Event(id,Stage.APPLIED));}
        else feedback.accept(new Event(id,Stage.DECLINED));
    }
    public void pop(UUID id){pending.remove(id);}
    public void clear(){pending.clear();}
    public int pendingCount(){return pending.size();}
}
