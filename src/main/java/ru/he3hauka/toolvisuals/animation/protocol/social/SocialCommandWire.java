package ru.he3hauka.toolvisuals.animation.protocol.social;

import java.nio.ByteBuffer;
import java.util.*;

import ru.he3hauka.toolvisuals.animation.PairEmote;
import ru.he3hauka.toolvisuals.animation.social.SocialProtocol;

public final class SocialCommandWire {
    public static final String REQUEST_CHANNEL="toolvisuals:social_command_v2",REPLY_CHANNEL="toolvisuals:social_reply_v2";
    public static final int MAX_REQUEST=34,MAX_REPLY=33;
    public enum Result { ACCEPTED(0),STALE(1),UNREGISTERED(2),UNAVAILABLE(3); public final int id; Result(int id){this.id=id;} }
    public record Request(long id,long epoch,SocialProtocol.Request action,long through) {
        public Request {
            if(id<=0||epoch<=0)throw new IllegalArgumentException("Invalid social identity");
            if(action instanceof SocialProtocol.Cancel){if(through<=0||through>=id)throw new IllegalArgumentException("Invalid cancellation fence");}
            else if(!(action instanceof SocialProtocol.Invite||action instanceof SocialProtocol.Answer)||through!=0)throw new IllegalArgumentException("Invalid social operation");
        }
    }
    public record Reply(UUID server,long id,long epoch,Result result) {
        public Reply {Objects.requireNonNull(server);Objects.requireNonNull(result);if(id<=0||epoch<=0)throw new IllegalArgumentException("Invalid social reply");}
    }
    private SocialCommandWire(){}
    public static byte[] encode(Request request){
        var b=ByteBuffer.allocate(MAX_REQUEST);int op=request.action instanceof SocialProtocol.Invite?0:request.action instanceof SocialProtocol.Answer?1:2;
        b.put((byte)op).putLong(request.id).putLong(request.epoch);
        if(request.action instanceof SocialProtocol.Invite invite){uuid(b,invite.target());b.put((byte)invite.emote().id);}
        else if(request.action instanceof SocialProtocol.Answer answer){uuid(b,answer.invitation());b.put((byte)(answer.accept()?1:0));}
        else b.putLong(request.through);
        return Arrays.copyOf(b.array(),b.position());
    }
    public static Request request(byte[] bytes){
        var b=input(bytes,MAX_REQUEST);int op=Byte.toUnsignedInt(b.get());long id=b.getLong(),epoch=b.getLong();
        SocialProtocol.Request action;long through=0;
        if(op==0){var target=uuid(b);var emote=PairEmote.byId(Byte.toUnsignedInt(b.get()));if(emote==null)throw new IllegalArgumentException("Unknown pair emote");action=new SocialProtocol.Invite(target,emote);}
        else if(op==1){var nonce=uuid(b);int accept=Byte.toUnsignedInt(b.get());if(accept>1)throw new IllegalArgumentException("Invalid consent");action=new SocialProtocol.Answer(nonce,accept==1);}
        else if(op==2){action=new SocialProtocol.Cancel();through=b.getLong();}
        else throw new IllegalArgumentException("Unknown social operation");
        var value=new Request(id,epoch,action,through);end(b);return value;
    }
    public static byte[] encode(Reply reply){var b=ByteBuffer.allocate(MAX_REPLY);uuid(b,reply.server);b.putLong(reply.id).putLong(reply.epoch).put((byte)reply.result.id);return b.array();}
    public static Reply reply(byte[] bytes){var b=input(bytes,MAX_REPLY);var server=uuid(b);long id=b.getLong(),epoch=b.getLong();int result=Byte.toUnsignedInt(b.get());var value=Arrays.stream(Result.values()).filter(v->v.id==result).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown social result"));end(b);return new Reply(server,id,epoch,value);}
    private static ByteBuffer input(byte[] bytes,int maximum){if(bytes==null||bytes.length<1||bytes.length>maximum)throw new IllegalArgumentException("Invalid social packet size");return ByteBuffer.wrap(bytes);}
    private static void end(ByteBuffer b){if(b.hasRemaining())throw new IllegalArgumentException("Trailing social bytes");}
    private static void uuid(ByteBuffer b,UUID id){b.putLong(id.getMostSignificantBits()).putLong(id.getLeastSignificantBits());}
    private static UUID uuid(ByteBuffer b){return new UUID(b.getLong(),b.getLong());}
}
