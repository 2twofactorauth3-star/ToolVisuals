package ru.he3hauka.toolvisuals.network.ingress;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ru.he3hauka.toolvisuals.animation.network.EmoteWire;
import ru.he3hauka.toolvisuals.animation.network.PacketBudget;
import ru.he3hauka.toolvisuals.animation.protocol.PlaybackWire;
import ru.he3hauka.toolvisuals.animation.protocol.social.SocialCommandWire;
import ru.he3hauka.toolvisuals.animation.social.SocialProtocol;
import ru.he3hauka.toolvisuals.cosmetic.accessory.AccessoryWire;
import ru.he3hauka.toolvisuals.cosmetic.cape.CapeWire;
import ru.he3hauka.toolvisuals.cosmetic.cape.CapeTransferWire;
import ru.he3hauka.toolvisuals.network.presence.PresenceWire;

public final class RequestAdmission {
    public enum Channel {
        SOLO_V1(EmoteWire.REQUEST_CHANNEL,EmoteWire.MAX_REQUEST_BYTES),
        SOLO_V2(EmoteWire.EXTENDED_REQUEST_CHANNEL,EmoteWire.MAX_REQUEST_BYTES),
        SOCIAL(SocialProtocol.REQUEST_CHANNEL,SocialProtocol.MAX_REQUEST_BYTES),
        PLAYBACK(PlaybackWire.REQUEST_CHANNEL,PlaybackWire.MAX_REQUEST_BYTES),
        CAPE(CapeWire.REQUEST_CHANNEL,CapeTransferWire.PACKET_LIMIT),
        ANIMATED_CAPE(CapeWire.ANIMATED_REQUEST_CHANNEL,CapeTransferWire.PACKET_LIMIT),
        CAPE_FRAGMENT(CapeTransferWire.REQUEST_CHANNEL,CapeTransferWire.MAX_REQUEST),
        FRIENDS(ru.he3hauka.toolvisuals.network.friends.FriendWire.REQUEST_CHANNEL,ru.he3hauka.toolvisuals.network.friends.FriendWire.MAX_REQUEST),
        PRESENCE(PresenceWire.REQUEST_CHANNEL,1),
        ACCESSORY(AccessoryWire.REQUEST_CHANNEL,AccessoryWire.MAX_REQUEST),
        SOCIAL_COMMAND(SocialCommandWire.REQUEST_CHANNEL,SocialCommandWire.MAX_REQUEST),
        MINI_GAMES(ru.he3hauka.toolvisuals.minigame.network.MiniGameWire.REQUEST_CHANNEL,ru.he3hauka.toolvisuals.minigame.network.MiniGameWire.MAX_REQUEST);

        public final String id;
        public final int maximumBytes;
        private static final Channel[] CHANNELS=values();
        Channel(String id,int maximumBytes){this.id=id;this.maximumBytes=maximumBytes;}
        public static Channel byId(String id){for(var channel:CHANNELS)if(channel.id.equals(id))return channel;return null;}
    }
    public enum Decision { ACCEPTED, MALFORMED, THROTTLED }
    public static final int MAX_CONNECTIONS=4096,CONNECTION_BURST=64,CONNECTION_RATE=32,GLOBAL_BURST=2048,GLOBAL_RATE=16384;
    private final Map<UUID,PacketBudget> connections=new HashMap<>();
    private final PacketBudget global;

    public RequestAdmission(long now){global=new PacketBudget(GLOBAL_BURST,GLOBAL_RATE,now);}

    public synchronized Decision admit(UUID player,Channel channel,int bytes,long now) {
        var budget=connections.get(player);
        if(budget!=null&&!budget.available(now)||!global.available(now))return Decision.THROTTLED;
        if(budget==null) {
            if(connections.size()>=MAX_CONNECTIONS)return Decision.THROTTLED;
            budget=new PacketBudget(CONNECTION_BURST,CONNECTION_RATE,now);connections.put(player,budget);
        }
        budget.allow(now);global.allow(now);
        return bytes>0&&bytes<=channel.maximumBytes?Decision.ACCEPTED:Decision.MALFORMED;
    }

    public synchronized void remove(UUID player){connections.remove(player);}
    public synchronized void clear(){connections.clear();}
}
