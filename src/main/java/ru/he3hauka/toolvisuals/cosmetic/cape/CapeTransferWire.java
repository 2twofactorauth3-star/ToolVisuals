package ru.he3hauka.toolvisuals.cosmetic.cape;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import ru.he3hauka.toolvisuals.network.fragment.PacketFragments;

public final class CapeTransferWire {
    public static final String REQUEST_CHANNEL="toolvisuals:cape_request_v3",STATE_CHANNEL="toolvisuals:cape_state_v3";
    public static final int MAX_REQUEST=16+PacketFragments.HEADER_BYTES+PacketFragments.DATA_BYTES;
    public static final int MAX_STATE=48+PacketFragments.HEADER_BYTES+PacketFragments.DATA_BYTES;
    public record RequestPart(long requestId,long epoch,PacketFragments.Part fragment){
        public RequestPart{CapeWire.positive(requestId);CapeWire.positive(epoch);Objects.requireNonNull(fragment);if(fragment.total()<18||fragment.total()>CapeWire.MAX_ANIMATED_REQUEST)throw new IllegalArgumentException("Invalid cape request transfer size");}
    }
    public record StatePart(UUID server,UUID actor,long epoch,long revision,PacketFragments.Part fragment){
        public StatePart{Objects.requireNonNull(server);Objects.requireNonNull(actor);CapeWire.positive(epoch);if(revision<0)throw new IllegalArgumentException("Invalid cape revision");Objects.requireNonNull(fragment);if(fragment.total()<49||fragment.total()>CapeWire.MAX_ANIMATED_STATE)throw new IllegalArgumentException("Invalid cape state transfer size");}
    }
    private CapeTransferWire(){}
    public static List<RequestPart> split(CapeWire.Request value){return PacketFragments.split(CapeWire.encodeAnimated(value)).stream().map(part->new RequestPart(value.requestId(),value.epoch(),part)).toList();}
    public static List<StatePart> split(CapeWire.State value){return PacketFragments.split(CapeWire.encodeAnimated(value)).stream().map(part->new StatePart(value.server(),value.actor(),value.epoch(),value.revision(),part)).toList();}
    public static byte[] encode(RequestPart value){var buffer=ByteBuffer.allocate(16+value.fragment.size());buffer.putLong(value.requestId).putLong(value.epoch);PacketFragments.write(buffer,value.fragment);return buffer.array();}
    public static byte[] encode(StatePart value){var buffer=ByteBuffer.allocate(48+value.fragment.size());CapeWire.uuid(buffer,value.server);CapeWire.uuid(buffer,value.actor);buffer.putLong(value.epoch).putLong(value.revision);PacketFragments.write(buffer,value.fragment);return buffer.array();}
    private static ByteBuffer input(byte[] bytes,int header,int maximum){if(bytes==null||bytes.length<header+PacketFragments.HEADER_BYTES+1||bytes.length>maximum)throw new IllegalArgumentException("Invalid cape fragment size");return ByteBuffer.wrap(bytes);}
    public static RequestPart request(byte[] bytes){var buffer=input(bytes,16,MAX_REQUEST);return new RequestPart(buffer.getLong(),buffer.getLong(),PacketFragments.read(buffer,CapeWire.MAX_ANIMATED_REQUEST));}
    public static StatePart state(byte[] bytes){var buffer=input(bytes,48,MAX_STATE);return new StatePart(CapeWire.uuid(buffer),CapeWire.uuid(buffer),buffer.getLong(),buffer.getLong(),PacketFragments.read(buffer,CapeWire.MAX_ANIMATED_STATE));}
    public static CapeWire.Request assembled(RequestPart part,byte[] bytes){var value=CapeWire.animatedRequest(bytes);if(value.requestId()!=part.requestId||value.epoch()!=part.epoch)throw new IllegalArgumentException("Cape request transfer identity mismatch");return value;}
    public static CapeWire.State assembled(StatePart part,byte[] bytes){var value=CapeWire.animatedState(bytes);if(!value.server().equals(part.server)||!value.actor().equals(part.actor)||value.epoch()!=part.epoch||value.revision()!=part.revision)throw new IllegalArgumentException("Cape state transfer identity mismatch");return value;}
    public static CapeWire.Request legacy(CapeWire.Request value){return value.design()==null||value.design().frameCount==1?value:new CapeWire.Request(value.requestId(),value.epoch(),value.receive(),value.design().frame(0));}
    public static CapeWire.State legacy(CapeWire.State value){return value.design()==null||value.design().frameCount==1?value:new CapeWire.State(value.server(),value.actor(),value.epoch(),value.revision(),value.design().frame(0));}
}
