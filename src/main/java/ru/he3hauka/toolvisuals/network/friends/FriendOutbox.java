package ru.he3hauka.toolvisuals.network.friends;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;

public final class FriendOutbox {
    public final UUID session=UUID.randomUUID();
    private final LongSupplier sequence;
    private final Map<UUID,Integer> confirmed=new LinkedHashMap<>();
    private FriendWire.Request pending;
    private boolean ready,stopping,closed;
    private long sent=Long.MIN_VALUE,acknowledged;
    public FriendOutbox(LongSupplier sequence){this.sequence=sequence;}
    public boolean closed(){return closed;}
    public long acknowledged(){return acknowledged;}
    public void stop(){if(stopping||closed)return;stopping=true;pending=null;ready=false;sent=Long.MIN_VALUE;}
    public FriendWire.Request next(Map<UUID,Integer> desired,long now){
        if(closed)return null;
        if(pending==null){
            FriendWire.Operation operation=null;UUID target=FriendWire.NONE;int fields=0;
            if(stopping)operation=FriendWire.Operation.BYE;
            else if(!ready)operation=FriendWire.Operation.HELLO;
            else {
                for(var old:confirmed.keySet())if(!desired.containsKey(old)){operation=FriendWire.Operation.REMOVE;target=old;break;}
                if(operation==null)for(var entry:desired.entrySet())if(!entry.getValue().equals(confirmed.get(entry.getKey()))){operation=FriendWire.Operation.CONTACT;target=entry.getKey();fields=entry.getValue();break;}
                if(operation==null&&now-acknowledged>=3_000_000_000L)operation=FriendWire.Operation.KEEP;
            }
            if(operation!=null){pending=new FriendWire.Request(session,sequence.getAsLong(),operation,target,fields);sent=Long.MIN_VALUE;}
        }
        if(pending==null||sent!=Long.MIN_VALUE&&now-sent<1_000_000_000L)return null;
        sent=now;return pending;
    }
    public boolean acknowledge(FriendWire.State reply,long now){
        if(reply.kind()!=FriendWire.Kind.REPLY||pending==null||!session.equals(reply.session())||reply.sequence()!=pending.id())return false;
        if(reply.fields()==0){closed=true;return true;}
        switch(pending.operation()){
            case HELLO->ready=true;
            case CONTACT->confirmed.put(pending.target(),pending.fields());
            case REMOVE->confirmed.remove(pending.target());
            case KEEP->{}
            case BYE->closed=true;
        }
        acknowledged=now;pending=null;return true;
    }
}
