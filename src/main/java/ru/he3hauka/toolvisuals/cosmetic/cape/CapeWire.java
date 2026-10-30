package ru.he3hauka.toolvisuals.cosmetic.cape;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;

import ru.he3hauka.toolvisuals.cosmetic.sync.CosmeticState;

public final class CapeWire {
    public static final String REQUEST_CHANNEL="toolvisuals:cape_request_v1",STATE_CHANNEL="toolvisuals:cape_state_v1",ACK_CHANNEL="toolvisuals:cape_ack_v1";
    public static final String ANIMATED_REQUEST_CHANNEL="toolvisuals:cape_request_v2",ANIMATED_STATE_CHANNEL="toolvisuals:cape_state_v2";
    public static final int MAX_REQUEST=18+CapeDesign.BYTES,MAX_STATE=49+CapeDesign.BYTES,MAX_ACK=41;
    public static final int MAX_ANIMATED_REQUEST=18+CapeDesign.MAX_ANIMATION_BYTES,MAX_ANIMATED_STATE=49+CapeDesign.MAX_ANIMATION_BYTES;
    public record Request(long requestId,long epoch,boolean receive,CapeDesign design){public Request{positive(requestId);positive(epoch);}}
    public record State(UUID server,UUID actor,long epoch,long revision,CapeDesign design) implements CosmeticState<CapeDesign>{public State{Objects.requireNonNull(server);Objects.requireNonNull(actor);positive(epoch);if(revision<0)throw new IllegalArgumentException("Invalid cape revision");}}
    public record Ack(UUID server,long requestId,long epoch,long revision,boolean accepted){public Ack{Objects.requireNonNull(server);positive(requestId);positive(epoch);if(revision<0)throw new IllegalArgumentException("Invalid cape revision");}}
    private CapeWire(){}
    public static byte[] encode(Request request){var buffer=ByteBuffer.allocate(request.design==null?18:MAX_REQUEST);buffer.putLong(request.requestId).putLong(request.epoch).put((byte)(request.receive?1:0));design(buffer,request.design);return buffer.array();}
    public static void checkRequest(byte[] bytes){
        if(bytes==null||bytes.length<18||bytes.length>MAX_REQUEST)throw new IllegalArgumentException("Invalid cape request size");
        var header=ByteBuffer.wrap(bytes);positive(header.getLong());positive(header.getLong());bool(header);boolean present=bool(header);
        if(bytes.length!=(present?MAX_REQUEST:18))throw new IllegalArgumentException("Invalid cape pixels");
    }
    public static Request request(byte[] bytes){checkRequest(bytes);var buffer=input(bytes,MAX_REQUEST);var value=new Request(buffer.getLong(),buffer.getLong(),bool(buffer),design(buffer));end(buffer);return value;}
    public static byte[] encodeAnimated(Request request){var pixels=request.design==null?null:request.design.animationBytes();var buffer=ByteBuffer.allocate(18+(pixels==null?0:pixels.length));buffer.putLong(request.requestId).putLong(request.epoch).put((byte)(request.receive?1:0));animation(buffer,pixels);return buffer.array();}
    public static void checkAnimatedRequest(byte[] bytes){
        if(bytes==null||bytes.length<18||bytes.length>MAX_ANIMATED_REQUEST)throw new IllegalArgumentException("Invalid animated cape request size");
        var buffer=ByteBuffer.wrap(bytes);positive(buffer.getLong());positive(buffer.getLong());bool(buffer);checkAnimation(buffer);
    }
    public static Request animatedRequest(byte[] bytes){checkAnimatedRequest(bytes);var buffer=input(bytes,MAX_ANIMATED_REQUEST);var value=new Request(buffer.getLong(),buffer.getLong(),bool(buffer),animation(buffer));end(buffer);return value;}
    public static byte[] encode(State state){var buffer=ByteBuffer.allocate(state.design==null?49:MAX_STATE);uuid(buffer,state.server);uuid(buffer,state.actor);buffer.putLong(state.epoch).putLong(state.revision);design(buffer,state.design);return buffer.array();}
    public static State state(byte[] bytes){var buffer=input(bytes,MAX_STATE);var value=new State(uuid(buffer),uuid(buffer),buffer.getLong(),buffer.getLong(),design(buffer));end(buffer);return value;}
    public static byte[] encodeAnimated(State state){var pixels=state.design==null?null:state.design.animationBytes();var buffer=ByteBuffer.allocate(49+(pixels==null?0:pixels.length));uuid(buffer,state.server);uuid(buffer,state.actor);buffer.putLong(state.epoch).putLong(state.revision);animation(buffer,pixels);return buffer.array();}
    public static State animatedState(byte[] bytes){var buffer=input(bytes,MAX_ANIMATED_STATE);var value=new State(uuid(buffer),uuid(buffer),buffer.getLong(),buffer.getLong(),animation(buffer));end(buffer);return value;}
    public static byte[] encode(Ack ack){var buffer=ByteBuffer.allocate(MAX_ACK);uuid(buffer,ack.server);buffer.putLong(ack.requestId).putLong(ack.epoch).putLong(ack.revision).put((byte)(ack.accepted?1:0));return buffer.array();}
    public static Ack ack(byte[] bytes){var buffer=input(bytes,MAX_ACK);var value=new Ack(uuid(buffer),buffer.getLong(),buffer.getLong(),buffer.getLong(),bool(buffer));end(buffer);return value;}
    private static void design(ByteBuffer buffer,CapeDesign design){buffer.put((byte)(design==null?0:1));if(design!=null)buffer.put(design.bytes());}
    private static CapeDesign design(ByteBuffer buffer){if(!bool(buffer))return null;if(buffer.remaining()!=CapeDesign.BYTES)throw new IllegalArgumentException("Invalid cape pixels");byte[] bytes=new byte[CapeDesign.BYTES];buffer.get(bytes);return CapeDesign.decode(bytes);}
    private static void animation(ByteBuffer buffer,byte[] pixels){buffer.put((byte)(pixels==null?0:1));if(pixels!=null)buffer.put(pixels);}
    private static void checkAnimation(ByteBuffer buffer){
        boolean present=bool(buffer);if(!present){end(buffer);return;}
        if(buffer.remaining()<2)throw new IllegalArgumentException("Missing cape animation frames");
        int count=Byte.toUnsignedInt(buffer.get()),fps=Byte.toUnsignedInt(buffer.get());
        if(count<1||count>CapeDesign.MAX_FRAMES||fps<1||fps>CapeDesign.MAX_FPS||buffer.remaining()!=count*CapeDesign.BYTES)throw new IllegalArgumentException("Invalid cape animation frames");
    }
    private static CapeDesign animation(ByteBuffer buffer){if(!bool(buffer))return null;byte[] bytes=new byte[buffer.remaining()];buffer.get(bytes);return CapeDesign.animation(bytes);}
    private static ByteBuffer input(byte[] bytes,int maximum){if(bytes==null||bytes.length==0||bytes.length>maximum)throw new IllegalArgumentException("Invalid cape payload size");return ByteBuffer.wrap(bytes);}
    private static boolean bool(ByteBuffer buffer){int value=Byte.toUnsignedInt(buffer.get());if(value>1)throw new IllegalArgumentException("Invalid cape flag");return value==1;}
    private static void positive(long value){if(value<=0)throw new IllegalArgumentException("Invalid cape identity");}
    private static void uuid(ByteBuffer buffer,UUID value){buffer.putLong(value.getMostSignificantBits()).putLong(value.getLeastSignificantBits());}
    private static UUID uuid(ByteBuffer buffer){return new UUID(buffer.getLong(),buffer.getLong());}
    private static void end(ByteBuffer buffer){if(buffer.hasRemaining())throw new IllegalArgumentException("Trailing cape bytes");}
}
