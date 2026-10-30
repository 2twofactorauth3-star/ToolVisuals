package ru.he3hauka.toolvisuals.client.social;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;

import ru.he3hauka.toolvisuals.animation.PairEmote;
import ru.he3hauka.toolvisuals.animation.network.SocialCommandPayload;
import ru.he3hauka.toolvisuals.animation.network.SocialPayload;
import ru.he3hauka.toolvisuals.animation.protocol.social.*;
import ru.he3hauka.toolvisuals.animation.social.SocialProtocol;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.animation.Emotes;
import ru.he3hauka.toolvisuals.client.animation.PlaybackClient;
import ru.he3hauka.toolvisuals.client.player.ClientActions;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.*;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class SocialClient {
    private static final Set<UUID> BADGES = new HashSet<>();
    private static Set<UUID> badgeSnapshot=Set.of();
    private static final java.util.Map<UUID, Long> STOPPED = new java.util.LinkedHashMap<>();
    private static Object connection;
    private static Object life;

    private static long invitationExpires;
    private static SocialProtocol.Invitation invitation;
    private static final SocialCommandQueue COMMANDS=new SocialCommandQueue();
    private static long epoch;
    private static UUID server;
    private SocialClient() {}

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(SocialPayload.Request.TYPE, SocialPayload.Request.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SocialPayload.State.TYPE, SocialPayload.State.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SocialCommandPayload.Request.TYPE,SocialCommandPayload.Request.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SocialCommandPayload.Reply.TYPE,SocialCommandPayload.Reply.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(SocialCommandPayload.Reply.TYPE,(payload,context)->{
            scope(context.client());
            var pending=COMMANDS.pending();var reply=payload.value();
            if(!COMMANDS.acknowledge(PlaybackClient.server(),PlaybackClient.epoch(),reply))return;
            if(pending.action() instanceof SocialProtocol.Answer answer&&invitation!=null&&invitation.id().equals(answer.invitation()))invitation=null;
            if(reply.result()!=SocialCommandWire.Result.ACCEPTED){COMMANDS.clear();PlaybackClient.revokeConsent();ClientActions.notice("pairs.command_failed");}
        });
        ClientPlayNetworking.registerGlobalReceiver(SocialPayload.State.TYPE, (payload, context) -> receive(payload.value()));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> PairInvitationChat.register(dispatcher, SocialClient::answerFromChat,
                () -> ToolVisualsClient.CONFIG.available(ANIMATIONS)));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
        ru.he3hauka.toolvisuals.client.replay.integration.ReplayLifecycle.unload((entity, world) -> badge(entity.getUUID(),false));
    }
    private static void reset() { BADGES.clear();badgeSnapshot=Set.of(); STOPPED.clear(); connection = null; life = null; invitation = null; COMMANDS.clear();epoch=0;server=null; }
    private static void scope(Minecraft client){
        if(connection!=client.getConnection()){reset();connection=client.getConnection();}
        if(life!=client.player||epoch!=PlaybackClient.epoch()||!java.util.Objects.equals(server,PlaybackClient.server())){
            life=client.player;epoch=PlaybackClient.epoch();server=PlaybackClient.server();invitation=null;STOPPED.clear();COMMANDS.clear();
        }
    }
    public static Set<UUID> badges(){if(badgeSnapshot==null)badgeSnapshot=Set.copyOf(BADGES);return badgeSnapshot;}
    private static void badge(UUID player,boolean visible){
        boolean changed=visible?BADGES.size()<2048&&BADGES.add(player):BADGES.remove(player);
        if(changed)badgeSnapshot=null;
    }
    public static boolean available() { return PlaybackClient.available() && ClientPlayNetworking.canSend(SocialCommandPayload.Request.TYPE); }
    private static boolean submit(SocialProtocol.Request request){
        scope(Minecraft.getInstance());
        if(!available()){ClientActions.notice("pairs.result.unavailable");return false;}
        if(COMMANDS.busy()){ClientActions.notice("pairs.command_waiting");return false;}
        COMMANDS.submit(PlaybackClient.epoch(),request,System.nanoTime());return true;
    }
    public static void withdraw(){scope(Minecraft.getInstance());if(PlaybackClient.epoch()>0)COMMANDS.revoke(PlaybackClient.epoch(),System.nanoTime());invitation=null;}
    public static void started(UUID partner,UUID pair){
        if(COMMANDS.confirmPair(partner,pair)&&invitation!=null&&invitation.id().equals(pair))invitation=null;
    }
    public static void cancel() { ru.he3hauka.toolvisuals.client.animation.PlaybackClient.stop(); }
    public static void invite(UUID target, PairEmote emote) {
        if (ToolVisualsClient.CONFIG.active(ANIMATIONS) && ToolVisualsClient.CONFIG.flag(EMOTE_SYNC)&&submit(new SocialProtocol.Invite(target,emote))) PlaybackClient.consent(target);
    }
    public static void answer(UUID id, boolean accept) {
        if (invitation == null || !invitation.id().equals(id)) return;
        boolean consent=accept&&System.nanoTime()<invitationExpires;
        if(submit(new SocialProtocol.Answer(id,consent))&&consent)PlaybackClient.consent(invitation.sender());
    }
    private static boolean answerFromChat(UUID id, boolean accept) {
        if (!pending(id) || !available() || !ToolVisualsClient.CONFIG.active(ANIMATIONS) || !ToolVisualsClient.CONFIG.flag(EMOTE_INVITES) || !ToolVisualsClient.CONFIG.flag(EMOTE_SYNC)) {
            ClientActions.notice("pairs.inactive");
            return false;
        }
        answer(id, accept);
        return true;
    }
    public static boolean pending(UUID id) { return invitation != null && invitation.id().equals(id) && System.nanoTime() < invitationExpires; }
    public static String name(UUID id) {
        var client = Minecraft.getInstance();
        var info = client.getConnection() == null ? null : client.getConnection().getPlayerInfo(id);
        return info == null ? "Player" : info.getProfile().name();
    }
    public static boolean badgeVisible(UUID player) {
        var client = Minecraft.getInstance();
        return ToolVisualsClient.CONFIG.active(CLIENT_BADGE)
                && (BADGES.contains(player) || client.player != null && client.player.getUUID().equals(player));
    }
    public static void tick(Minecraft client) {
        scope(client);
        if (client.player == null || client.level == null) return;
        long now = System.nanoTime();
        STOPPED.values().removeIf(stopped -> now - stopped >= 15_000_000_000L);
        if(!ToolVisualsClient.CONFIG.active(ANIMATIONS)||!ToolVisualsClient.CONFIG.flag(EMOTE_SYNC))withdraw();
        if(COMMANDS.timedOut(now)){COMMANDS.revoke(epoch,now);PlaybackClient.revokeConsent();invitation=null;ClientActions.notice("pairs.command_timeout");}
        if(available()){var request=COMMANDS.due(now);if(request!=null)ClientPlayNetworking.send(new SocialCommandPayload.Request(request));}
        if (!COMMANDS.busy()&&invitation != null && (!pending(invitation.id()) || !ToolVisualsClient.CONFIG.flag(EMOTE_INVITES))) answer(invitation.id(), false);
    }
    private static void receive(SocialProtocol.State state) {
        var client = Minecraft.getInstance();
        scope(client);
        if (client.player == null || client.level == null) return;
        if (state instanceof SocialProtocol.Badge badge) {
            if (!badge.visible()) badge(badge.player(),false);
            else if (BADGES.size() < 2048 && client.getConnection().getPlayerInfo(badge.player()) != null) badge(badge.player(),true);
        } else if (state instanceof SocialProtocol.Invitation next) {
            if (invitation != null || !ToolVisualsClient.CONFIG.active(ANIMATIONS) || !ToolVisualsClient.CONFIG.flag(EMOTE_INVITES) || !ToolVisualsClient.CONFIG.flag(EMOTE_SYNC)) {
                if(!COMMANDS.busy())submit(new SocialProtocol.Answer(next.id(),false));return;
            }
            if (client.level.getPlayerByUUID(next.sender()) == null) { if(!COMMANDS.busy())submit(new SocialProtocol.Answer(next.id(),false));return; }
            invitation = next; invitationExpires = System.nanoTime() + 10_000_000_000L;
            ru.he3hauka.toolvisuals.client.sound.SoundCustomizer.notice();
            ru.he3hauka.toolvisuals.client.chat.message.ChatMessages.send(PairInvitationChat.message(next, name(next.sender())));
        } else if (state instanceof SocialProtocol.Started started) {
            if (ru.he3hauka.toolvisuals.client.animation.PlaybackClient.supported()) return;
            if (STOPPED.containsKey(started.id())) return;
            if (invitation != null && invitation.id().equals(started.id())) invitation = null;
            Emotes.startPair(started);
        } else if (state instanceof SocialProtocol.Stopped stopped) {
            if (ru.he3hauka.toolvisuals.client.animation.PlaybackClient.supported()) return;
            if (STOPPED.size() >= 256) STOPPED.remove(STOPPED.keySet().iterator().next());
            STOPPED.put(stopped.id(), System.nanoTime());
            Emotes.stopPair(stopped.id());
        }
        else if (state instanceof SocialProtocol.Result result) {
            if (invitation != null && invitation.id().equals(result.id()) && result.outcome() != SocialProtocol.Outcome.SENT) invitation = null;
            ClientActions.notice("pairs.result." + result.outcome().name().toLowerCase(java.util.Locale.ROOT));
        }
    }
}
