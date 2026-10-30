package ru.he3hauka.toolvisuals.animation.network;

import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

import ru.he3hauka.toolvisuals.client.cape.network.CapePayload;
import ru.he3hauka.toolvisuals.client.cosmetic.accessory.network.AccessoryPayload;
import ru.he3hauka.toolvisuals.client.social.presence.network.PresencePayload;
import ru.he3hauka.toolvisuals.cosmetic.accessory.AccessoryWire;
import ru.he3hauka.toolvisuals.cosmetic.cape.CapeWire;
import ru.he3hauka.toolvisuals.network.presence.PresenceWire;

public final class EmotePacketGuard {
    private final PacketBudget requests;
    private final PacketBudget states;
    private final PacketBudget controls,assets,assetRequests,bytes;

    public EmotePacketGuard(long now) {
        requests = new PacketBudget(12, 8, now);
        states = new PacketBudget(2048, 1024, now);
        controls=new PacketBudget(512,256,now);
        assets=new PacketBudget(1024,512,now);
        assetRequests=new PacketBudget(16,8,now);
        bytes=new PacketBudget(4*1024*1024,2*1024*1024,now);
    }

    public boolean allow(Packet<?> packet, long now) {
        if (packet instanceof BundlePacket<?> bundle) {
            for (var part : bundle.subPackets()) if (!allowSingle(part, now)) return false;
            return true;
        }
        return allowSingle(packet, now);
    }

    private boolean allowSingle(Packet<?> packet, long now) {
        if (packet instanceof ServerboundCustomPayloadPacket request) {
            var value=request.payload();
            if(value instanceof ru.he3hauka.toolvisuals.client.minigame.network.MiniGamePayload.Request)return requests.allow(now);
            if(value instanceof EmoteRequestPayload||value instanceof SocialPayload.Request||value instanceof PlaybackPayload.Request||value instanceof SocialCommandPayload.Request)return requests.allow(now);
            if(value instanceof CapePayload.Request||value instanceof CapePayload.AnimatedRequest||value instanceof AccessoryPayload.Request||value instanceof PresencePayload.Request)return assetRequests.allow(now);
        }
        if (packet instanceof ClientboundCustomPayloadPacket state) {
            var value=state.payload();
            if(value instanceof ru.he3hauka.toolvisuals.client.minigame.network.MiniGamePayload.State)return controls.allow(now)&&bytes.allow(now,ru.he3hauka.toolvisuals.minigame.network.MiniGameWire.MAX_STATE);
            if(value instanceof ru.he3hauka.toolvisuals.client.presents.network.WorldContextMessage)return controls.allow(now)&&bytes.allow(now,ru.he3hauka.toolvisuals.network.world.WorldContextWire.MAX_NAMED_BYTES);
            if(value instanceof PlaybackPayload.Reply||value instanceof SocialCommandPayload.Reply||value instanceof CapePayload.Ack||value instanceof AccessoryPayload.Ack)return controls.allow(now);
            if(value instanceof PlaybackPayload.State playback&&playback.value().mode()==ru.he3hauka.toolvisuals.animation.protocol.PlaybackWire.Mode.STOPPED
                    ||value instanceof EmoteStatePayload emote&&emote.emote()==-1||value instanceof SocialPayload.State social&&social.value() instanceof ru.he3hauka.toolvisuals.animation.social.SocialProtocol.Stopped)return controls.allow(now);
            if(value instanceof EmoteStatePayload||value instanceof SocialPayload.State||value instanceof PlaybackPayload.State)return states.allow(now);
            int cost=value instanceof CapePayload.AnimatedState?CapeWire.MAX_ANIMATED_STATE:value instanceof CapePayload.State?CapeWire.MAX_STATE:value instanceof AccessoryPayload.State||value instanceof AccessoryPayload.ExpandedState?AccessoryWire.MAX_STATE:value instanceof PresencePayload.State?PresenceWire.MAX_BYTES:0;
            if(cost>0)return assets.allow(now)&&bytes.allow(now,cost);
        }
        return true;
    }
}
