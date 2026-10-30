package ru.he3hauka.toolvisuals.cosmetic.accessory;

import java.nio.ByteBuffer;
import java.util.*;

import ru.he3hauka.toolvisuals.cosmetic.style.ColorPalette;
import ru.he3hauka.toolvisuals.cosmetic.sync.CosmeticState;

public final class AccessoryWire {
    public static final String REQUEST_CHANNEL="toolvisuals:accessory_request_v1",STATE_CHANNEL="toolvisuals:accessory_state_v1",ACK_CHANNEL="toolvisuals:accessory_ack_v1";
    public static final String EXPANDED_STATE_CHANNEL="toolvisuals:accessory_state_v2";
    public static final int MAX_LOOK=15+2*(36+ColorPalette.LIMIT*4),MAX_REQUEST=18+MAX_LOOK,MAX_STATE=49+MAX_LOOK,MAX_ACK=41;
    public record Request(long requestId,long epoch,boolean receive,AccessoryLook design){public Request{positive(requestId);positive(epoch);}}
    public record State(UUID server,UUID actor,long epoch,long revision,AccessoryLook design) implements CosmeticState<AccessoryLook>{public State{Objects.requireNonNull(server);Objects.requireNonNull(actor);positive(epoch);if(revision<0)throw new IllegalArgumentException();}}
    public record Ack(UUID server,long requestId,long epoch,long revision,boolean accepted){public Ack{Objects.requireNonNull(server);positive(requestId);positive(epoch);if(revision<0)throw new IllegalArgumentException();}}
    private AccessoryWire(){}
    public static byte[] encode(Request value){var b=ByteBuffer.allocate(MAX_REQUEST);b.putLong(value.requestId).putLong(value.epoch).put((byte)(value.receive?1:0));look(b,value.design);return finish(b);}
    public static void checkRequest(byte[] bytes){
        if(bytes==null||bytes.length<18||bytes.length>MAX_REQUEST)throw new IllegalArgumentException("Invalid cosmetic request size");
        var b=ByteBuffer.wrap(bytes);positive(b.getLong());positive(b.getLong());bool(b);boolean present=bool(b);
        if(!present&&bytes.length!=18||present&&bytes.length<113)throw new IllegalArgumentException("Invalid cosmetic request body");
    }
    public static Request request(byte[] bytes){checkRequest(bytes);var b=input(bytes,MAX_REQUEST);var value=new Request(b.getLong(),b.getLong(),bool(b),look(b));end(b);return value;}
    public static byte[] encode(State value){var b=ByteBuffer.allocate(MAX_STATE);uuid(b,value.server);uuid(b,value.actor);b.putLong(value.epoch).putLong(value.revision);look(b,value.design);return finish(b);}
    public static State state(byte[] bytes){var b=input(bytes,MAX_STATE);var value=new State(uuid(b),uuid(b),b.getLong(),b.getLong(),look(b));end(b);return value;}
    public static State legacy(State value){
        if(value.design==null)return value;
        var compatible=value.design.legacy();
        return compatible==value.design?value:new State(value.server,value.actor,value.epoch,value.revision,compatible);
    }
    public static byte[] encode(Ack value){var b=ByteBuffer.allocate(MAX_ACK);uuid(b,value.server);b.putLong(value.requestId).putLong(value.epoch).putLong(value.revision).put((byte)(value.accepted?1:0));return b.array();}
    public static Ack ack(byte[] bytes){var b=input(bytes,MAX_ACK);var value=new Ack(uuid(b),b.getLong(),b.getLong(),b.getLong(),bool(b));end(b);return value;}
    public static byte[] encodeLook(AccessoryLook value){var b=ByteBuffer.allocate(MAX_LOOK);design(b,value);return finish(b);}
    public static AccessoryLook decodeLook(byte[] bytes){var b=input(bytes,MAX_LOOK);var value=design(b);end(b);return value;}
    private static void look(ByteBuffer b,AccessoryLook value){b.put((byte)(value==null?0:1));if(value!=null)design(b,value);}
    private static AccessoryLook look(ByteBuffer b){return bool(b)?design(b):null;}
    private static void design(ByteBuffer b,AccessoryLook value){b.put((byte)value.back().id).put((byte)value.head().id).putFloat(value.backScale()).putFloat(value.headScale()).putFloat(value.speed()).put((byte)(value.animated()?1:0));palette(b,value.backColors());palette(b,value.headColors());}
    private static AccessoryLook design(ByteBuffer b){var back=AccessoryLook.Back.byId(Byte.toUnsignedInt(b.get()));var head=AccessoryLook.Head.byId(Byte.toUnsignedInt(b.get()));float bs=b.getFloat(),hs=b.getFloat(),speed=b.getFloat();boolean animated=bool(b);return new AccessoryLook(back,head,palette(b),palette(b),bs,hs,speed,animated);}
    private static void palette(ByteBuffer b,ColorPalette p){b.put((byte)p.colors.size()).put((byte)p.mode.id).put((byte)p.space.id).putDouble(p.duration).putDouble(p.hold).putDouble(p.phase).putDouble(p.spread).put((byte)((p.smooth?1:0)|(p.reverse?2:0)));for(int color:p.colors)b.putInt(color);}
    private static ColorPalette palette(ByteBuffer b){
        int size=Byte.toUnsignedInt(b.get());if(size<1||size>ColorPalette.LIMIT)throw new IllegalArgumentException("Invalid palette length");
        var mode=ColorPalette.Mode.byId(Byte.toUnsignedInt(b.get()));var space=ColorPalette.Space.byId(Byte.toUnsignedInt(b.get()));
        double duration=b.getDouble(),hold=b.getDouble(),phase=b.getDouble(),spread=b.getDouble();int flags=Byte.toUnsignedInt(b.get());
        if(!Double.isFinite(duration)||duration<.2||duration>120||!Double.isFinite(hold)||hold<0||hold>.9||!Double.isFinite(phase)||phase<0||phase>1||!Double.isFinite(spread)||spread<0||spread>2||flags>3||b.remaining()<size*4)throw new IllegalArgumentException("Invalid palette");
        var colors=new ArrayList<Integer>(size);for(int i=0;i<size;i++){int rgb=b.getInt();if(rgb<0||rgb>0xffffff)throw new IllegalArgumentException("Invalid RGB color");colors.add(rgb);}
        return new ColorPalette(colors,mode,space,duration,hold,phase,spread,(flags&1)!=0,(flags&2)!=0);
    }
    private static byte[] finish(ByteBuffer b){return Arrays.copyOf(b.array(),b.position());}
    private static ByteBuffer input(byte[] bytes,int maximum){if(bytes==null||bytes.length==0||bytes.length>maximum)throw new IllegalArgumentException("Invalid cosmetic payload length");return ByteBuffer.wrap(bytes);}
    private static void uuid(ByteBuffer b,UUID value){b.putLong(value.getMostSignificantBits()).putLong(value.getLeastSignificantBits());}
    private static UUID uuid(ByteBuffer b){return new UUID(b.getLong(),b.getLong());}
    private static boolean bool(ByteBuffer b){int value=Byte.toUnsignedInt(b.get());if(value>1)throw new IllegalArgumentException("Invalid cosmetic flag");return value==1;}
    private static void positive(long value){if(value<=0)throw new IllegalArgumentException("Invalid cosmetic identity");}
    private static void end(ByteBuffer b){if(b.hasRemaining())throw new IllegalArgumentException("Trailing cosmetic bytes");}
}
