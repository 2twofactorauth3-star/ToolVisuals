package ru.he3hauka.toolvisuals.network.friends;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FriendSession {
    public static final long LEASE=12_000_000_000L;
    private final Map<UUID,Integer> contacts=new HashMap<>();
    private Map<UUID,Integer> view=Map.of();
    private UUID session=FriendWire.NONE;
    private FriendWire.Request last;
    private boolean accepted,active;
    private long seen;
    public synchronized UUID session(){return session;}
    public synchronized boolean active(long now){if(active&&now-seen>=LEASE){active=false;contacts.clear();view=Map.of();}return active;}
    public synchronized Map<UUID,Integer> contacts(long now){return active(now)?view:Map.of();}
    public synchronized int fields(UUID player,long now){return active(now)?contacts.getOrDefault(player,-1):-1;}
    public synchronized boolean apply(FriendWire.Request request,long now){
        if(last!=null&&request.id()<=last.id()){
            boolean duplicate=request.equals(last)&&accepted;if(duplicate&&active(now))seen=now;return duplicate;
        }
        boolean next=request.operation()==FriendWire.Operation.HELLO||active(now)&&session.equals(request.session());
        if(next)switch(request.operation()){
            case HELLO->{if(!session.equals(request.session()))contacts.clear();session=request.session();active=true;}
            case CONTACT->{if(!contacts.containsKey(request.target())&&contacts.size()>=FriendWire.MAX_FRIENDS)next=false;else contacts.put(request.target(),request.fields());}
            case REMOVE->contacts.remove(request.target());
            case KEEP->{}
            case BYE->{active=false;contacts.clear();}
        }
        last=request;accepted=next;if(next){seen=now;if(request.operation()!=FriendWire.Operation.KEEP)view=Map.copyOf(contacts);}return next;
    }
    public synchronized void close(){active=false;contacts.clear();view=Map.of();}
    public static boolean mutual(FriendSession first,UUID firstId,FriendSession second,UUID secondId,long now){
        return first!=null&&second!=null&&first.fields(secondId,now)>=0&&second.fields(firstId,now)>=0;
    }
}
