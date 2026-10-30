package ru.he3hauka.toolvisuals.animation.protocol;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import ru.he3hauka.toolvisuals.animation.Emote;
import ru.he3hauka.toolvisuals.animation.PairEmote;
import ru.he3hauka.toolvisuals.animation.network.WireCatalog;

public final class PlaybackWire {
    public static final String REQUEST_CHANNEL="toolvisuals:playback_request_v3", STATE_CHANNEL="toolvisuals:playback_state_v3", REPLY_CHANNEL="toolvisuals:playback_reply_v3";
    public static final int MAX_REQUEST_BYTES=33, MAX_STATE_BYTES=110, MAX_REPLY_BYTES=121;
    public enum Operation {
        HELLO(0),START(1),STOP(2),STOP_PAIR(3);
        public final int id;
        Operation(int id){this.id=id;}
        private static final Map<Integer,Operation> IDS=WireCatalog.index(values(),value->value.id);
    }
    public enum Mode {
        STOPPED(0),SOLO(1),PAIR(2);
        public final int id;
        Mode(int id){this.id=id;}
        private static final Map<Integer,Mode> IDS=WireCatalog.index(values(),value->value.id);
    }
    public enum Result {
        ACCEPTED(0),UNAVAILABLE(1),COOLDOWN(2),GLOBAL_LIMIT(3),STALE(4),UNREGISTERED(5);
        public final int id;
        Result(int id){this.id=id;}
        private static final Map<Integer,Result> IDS=WireCatalog.index(values(),value->value.id);
    }
    public sealed interface Request permits Hello,Start,Stop,StopPair {long requestId();default long epoch(){return 0;}}
    public record Hello(long requestId,boolean badge) implements Request {public Hello{positive(requestId);}}
    public record Start(long requestId,long epoch,int emote,boolean cancelMoving) implements Request {
        public Start{positive(requestId);positive(epoch);if(Emote.byId(emote)==null)throw new IllegalArgumentException("Unknown emote");}
    }
    public record Stop(long requestId,long epoch,long playbackId) implements Request {public Stop{positive(requestId);positive(epoch);positive(playbackId);}}
    public record StopPair(long requestId,long epoch,UUID pairId) implements Request {public StopPair{positive(requestId);positive(epoch);Objects.requireNonNull(pairId);}}
    public record Pair(UUID id,UUID partner,long partnerEpoch,long partnerRevision,boolean second) {
        public Pair{Objects.requireNonNull(id);Objects.requireNonNull(partner);positive(partnerEpoch);if(partnerRevision<0)throw new IllegalArgumentException("Invalid partner revision");}
    }
    public record State(UUID server,UUID actor,long epoch,long revision,Mode mode,long playbackId,int emote,int ageMillis,Pair pair) {
        public State {
            Objects.requireNonNull(server);Objects.requireNonNull(actor);Objects.requireNonNull(mode);positive(epoch);
            if(revision<0||playbackId<0||ageMillis<0)throw new IllegalArgumentException("Invalid playback identity");
            if(mode==Mode.STOPPED){if(emote!=-1||ageMillis!=0||pair!=null)throw new IllegalArgumentException("Invalid terminal state");}
            else if(mode==Mode.SOLO){var value=Emote.byId(emote);if(value==null||playbackId==0||pair!=null||ageMillis>=value.duration*50)throw new IllegalArgumentException("Invalid solo state");}
            else {var value=PairEmote.byId(emote);if(value==null||pair==null||playbackId!=0||actor.equals(pair.partner)||ageMillis>=value.animation.duration*50)throw new IllegalArgumentException("Invalid pair state");}
        }
        public State aged(int age){return mode==Mode.STOPPED?this:new State(server,actor,epoch,revision,mode,playbackId,emote,age,pair);}
        public int durationMillis(){return mode==Mode.STOPPED?0:mode==Mode.SOLO?Emote.byId(emote).duration*50:PairEmote.byId(emote).animation.duration*50;}
        public State partnerState(){if(pair==null)throw new IllegalStateException("Not a pair");return new State(server,pair.partner,pair.partnerEpoch,pair.partnerRevision,Mode.PAIR,0,emote,ageMillis,new Pair(pair.id,actor,epoch,revision,!pair.second));}
    }
    public record Reply(long requestId,Operation operation,Result result,boolean registered,State state) {
        public Reply{positive(requestId);Objects.requireNonNull(operation);Objects.requireNonNull(result);Objects.requireNonNull(state);}
    }
    private PlaybackWire(){}
    public static Operation operation(Request value){return value instanceof Hello?Operation.HELLO:value instanceof Start?Operation.START:value instanceof Stop?Operation.STOP:Operation.STOP_PAIR;}
    public static byte[] encode(Request value){
        var out=ByteBuffer.allocate(MAX_REQUEST_BYTES);out.put((byte)operation(value).id).putLong(value.requestId());
        if(!(value instanceof Hello))out.putLong(value.epoch());
        if(value instanceof Hello hello)bool(out,hello.badge);
        else if(value instanceof Start start){out.put((byte)start.emote);bool(out,start.cancelMoving);}
        else if(value instanceof Stop stop)out.putLong(stop.playbackId);
        else uuid(out,((StopPair)value).pairId);
        return finish(out);
    }
    public static Request decodeRequest(byte[] bytes){
        var in=input(bytes,MAX_REQUEST_BYTES);var operation=required(Operation.IDS,in.get());long id=in.getLong();long epoch=operation==Operation.HELLO?0:in.getLong();
        Request value=switch(operation){case HELLO->new Hello(id,bool(in));case START->new Start(id,epoch,Byte.toUnsignedInt(in.get()),bool(in));case STOP->new Stop(id,epoch,in.getLong());case STOP_PAIR->new StopPair(id,epoch,uuid(in));};
        end(in);return value;
    }
    public static byte[] encode(State value){var out=ByteBuffer.allocate(MAX_STATE_BYTES);state(out,value);return finish(out);}
    public static State decodeState(byte[] bytes){var in=input(bytes,MAX_STATE_BYTES);var state=state(in);end(in);return state;}
    public static byte[] encode(Reply value){var out=ByteBuffer.allocate(MAX_REPLY_BYTES);out.putLong(value.requestId).put((byte)value.operation.id).put((byte)value.result.id);bool(out,value.registered);state(out,value.state);return finish(out);}
    public static Reply decodeReply(byte[] bytes){
        var in=input(bytes,MAX_REPLY_BYTES);long request=in.getLong();var operation=required(Operation.IDS,in.get());var result=required(Result.IDS,in.get());boolean registered=bool(in);var reply=new Reply(request,operation,result,registered,state(in));end(in);return reply;
    }
    private static void state(ByteBuffer out,State value){
        uuid(out,value.server);uuid(out,value.actor);out.putLong(value.epoch).putLong(value.revision).put((byte)value.mode.id).putLong(value.playbackId).putShort((short)value.emote).putShort((short)value.ageMillis);
        if(value.pair!=null){uuid(out,value.pair.id);uuid(out,value.pair.partner);out.putLong(value.pair.partnerEpoch).putLong(value.pair.partnerRevision);bool(out,value.pair.second);}
    }
    private static State state(ByteBuffer in){
        UUID server=uuid(in),actor=uuid(in);long epoch=in.getLong(),revision=in.getLong();var mode=required(Mode.IDS,in.get());long playback=in.getLong();int emote=in.getShort(),age=Short.toUnsignedInt(in.getShort());
        Pair pair=mode==Mode.PAIR?new Pair(uuid(in),uuid(in),in.getLong(),in.getLong(),bool(in)):null;
        return new State(server,actor,epoch,revision,mode,playback,emote,age,pair);
    }
    private static <T>T required(Map<Integer,T> values,byte id){var value=values.get(Byte.toUnsignedInt(id));if(value==null)throw new IllegalArgumentException("Unknown protocol value");return value;}
    private static void positive(long value){if(value<=0)throw new IllegalArgumentException("Expected a positive identity");}
    private static void bool(ByteBuffer out,boolean value){out.put((byte)(value?1:0));}
    private static boolean bool(ByteBuffer in){int value=Byte.toUnsignedInt(in.get());if(value>1)throw new IllegalArgumentException("Invalid boolean");return value==1;}
    private static UUID uuid(ByteBuffer in){return new UUID(in.getLong(),in.getLong());}
    private static void uuid(ByteBuffer out,UUID id){out.putLong(id.getMostSignificantBits()).putLong(id.getLeastSignificantBits());}
    private static ByteBuffer input(byte[] bytes,int max){if(bytes==null||bytes.length==0||bytes.length>max)throw new IllegalArgumentException("Invalid payload size");return ByteBuffer.wrap(bytes);}
    private static void end(ByteBuffer in){if(in.hasRemaining())throw new IllegalArgumentException("Trailing protocol bytes");}
    private static byte[] finish(ByteBuffer out){return Arrays.copyOf(out.array(),out.position());}
}
