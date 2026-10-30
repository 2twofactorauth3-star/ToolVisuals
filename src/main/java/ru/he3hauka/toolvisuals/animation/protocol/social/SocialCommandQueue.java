package ru.he3hauka.toolvisuals.animation.protocol.social;

import java.util.UUID;

import ru.he3hauka.toolvisuals.animation.social.SocialProtocol;

public final class SocialCommandQueue {
    public static final long RETRY_NANOS=500_000_000L,TIMEOUT_NANOS=8_000_000_000L;
    private long sequence,activation,began,sent=Long.MIN_VALUE;
    private SocialCommandWire.Request pending;
    public boolean busy(){return pending!=null;}
    public SocialCommandWire.Request pending(){return pending;}
    public void submit(long epoch,SocialProtocol.Request action,long now){
        if(pending!=null)throw new IllegalStateException("A social command is awaiting acknowledgement");
        pending=new SocialCommandWire.Request(++sequence,epoch,action,0);activation=pending.id();began=now;sent=Long.MIN_VALUE;
    }
    public void revoke(long epoch,long now){
        if(activation==0||pending!=null&&pending.action() instanceof SocialProtocol.Cancel)return;
        pending=new SocialCommandWire.Request(++sequence,epoch,new SocialProtocol.Cancel(),activation);activation=0;began=now;sent=Long.MIN_VALUE;
    }
    public SocialCommandWire.Request due(long now){
        if(pending==null||sent!=Long.MIN_VALUE&&now-sent<RETRY_NANOS)return null;
        sent=now;return pending;
    }
    public boolean timedOut(long now){return pending!=null&&!(pending.action() instanceof SocialProtocol.Cancel)&&now-began>=TIMEOUT_NANOS;}
    public boolean confirmPair(UUID partner,UUID pair){
        if(pending==null)return false;
        boolean matches=pending.action() instanceof SocialProtocol.Invite invite&&invite.target().equals(partner)
                ||pending.action() instanceof SocialProtocol.Answer answer&&answer.accept()&&answer.invitation().equals(pair);
        if(matches)pending=null;
        return matches;
    }
    public boolean acknowledge(UUID server,long epoch,SocialCommandWire.Reply reply){
        if(pending==null||!reply.server().equals(server)||reply.epoch()!=epoch||reply.id()!=pending.id())return false;
        pending=null;return true;
    }
    public void clear(){pending=null;activation=0;sent=Long.MIN_VALUE;}
}
