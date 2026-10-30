package ru.he3hauka.toolvisuals.client.animation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import ru.he3hauka.toolvisuals.animation.Emote;
import ru.he3hauka.toolvisuals.animation.network.*;
import ru.he3hauka.toolvisuals.animation.protocol.PlaybackReplica.StopReason;
import ru.he3hauka.toolvisuals.animation.social.SocialProtocol;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.animation.ui.EmoteWheelScreen;
import ru.he3hauka.toolvisuals.client.player.ClientActions;
import ru.he3hauka.toolvisuals.client.social.SocialClient;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.*;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class Emotes {
    private static final EmoteTimeline TIMELINE = new EmoteTimeline();
    private static final EmoteClock CLOCK = new EmoteClock();
    private static final PairStagingTimeline STAGING = new PairStagingTimeline();
    private static Object level;
    private static Object localPlayer;
    private static final Map<UUID, PairStage> ANCHORS = new HashMap<>();
    private static double startX, startY, startZ;
    private static double lastStart = Double.NEGATIVE_INFINITY;
    private static boolean sharing;
    private static boolean synchronizedSetting=true;
    private static final Map<UUID, SocialProtocol.Started> PAIRS = new HashMap<>();
    private Emotes() {}

    public static void initialize() {
        PlaybackClient.initialize();
        var c2s = net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.serverboundPlay();
        var s2c = net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.clientboundPlay();
        c2s.register(EmoteRequestPayload.TYPE, EmoteRequestPayload.CODEC);
        c2s.register(EmoteRequestPayload.EXTENDED_TYPE, EmoteRequestPayload.EXTENDED_CODEC);
        s2c.register(EmoteStatePayload.TYPE, EmoteStatePayload.CODEC);
        s2c.register(EmoteStatePayload.EXTENDED_TYPE, EmoteStatePayload.EXTENDED_CODEC);
        ClientPlayNetworking.registerGlobalReceiver(EmoteStatePayload.TYPE, (payload, context) -> receive(payload));
        ClientPlayNetworking.registerGlobalReceiver(EmoteStatePayload.EXTENDED_TYPE, (payload, context) -> receive(payload));
        ru.he3hauka.toolvisuals.client.replay.integration.ReplayLifecycle.unload((entity, world) -> { TIMELINE.remove(entity.getUUID()); STAGING.remove(entity.getUUID()); PAIRS.remove(entity.getUUID()); PlaybackClient.unload(entity.getUUID()); });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            PlaybackClient.disconnect();
            sharing = false;
            ANCHORS.clear();
            TIMELINE.clear();
            STAGING.clear();
            PAIRS.clear();
            restoreCamera(client);
            level = null;
        });
    }

    private static void scope(Minecraft client) {
        if (level == client.level && localPlayer == client.player) return;
        localPlayer = client.player;
        TIMELINE.protect(client.player==null?null:client.player.getUUID());
        ANCHORS.clear();
        restoreCamera(client);
        TIMELINE.clear();
        STAGING.clear();
        PAIRS.clear();
        CLOCK.reset(System.nanoTime());
        level = client.level;
        lastStart = Double.NEGATIVE_INFINITY;
        sharing = false;
        synchronizedSetting=ToolVisualsClient.CONFIG.flag(EMOTE_SYNC);
    }

    private static double time(Minecraft client) {
        scope(client);
        return CLOCK.sample(System.nanoTime(), client.isPaused());
    }

    public static boolean synchronizedServer() { return PlaybackClient.available(); }
    public static boolean synchronizedServer(Emote emote) { return synchronizedServer(); }

    public static void start(Emote emote) {
        var client = Minecraft.getInstance();
        double clock = time(client);
        if (emote == null || client.player == null || client.level == null || !ToolVisualsClient.CONFIG.active(ANIMATIONS)) return;
        var player = client.player;
        if (!player.isAlive() || player.isSpectator() || player.isSleeping() || player.isFallFlying() || player.isPassenger() || player.hurtTime > 0) {
            ClientActions.notice("emotes.unavailable");
            return;
        }
        if (clock - lastStart < 10) return;
        SocialClient.withdraw();
        if (PAIRS.containsKey(player.getUUID())) { SocialClient.cancel(); clearPair(player.getUUID(), clock); }
        boolean synchronize = ToolVisualsClient.CONFIG.flag(EMOTE_SYNC) && synchronizedServer(emote);
        lastStart = clock;
        TIMELINE.start(player.getUUID(), emote, clock, 0);
        if(ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.action(ANIMATIONS,"solo_started","emote_id="+emote.id+" synchronized="+synchronize);
        startX = player.getX();
        startY = player.getY();
        startZ = player.getZ();
        camera(client);
        sharing = synchronize;
        PlaybackClient.start(emote.id, ToolVisualsClient.CONFIG.flag(EMOTE_CANCEL_MOVING), synchronize);
    }

    public static void stop(boolean notify) {
        var client = Minecraft.getInstance();
        stop(client, time(client), notify,StopReason.USER);
    }

    private static void stop(Minecraft client, double clock, boolean notify,StopReason reason) {
        if(ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active()&&client.player!=null&&TIMELINE.current(client.player.getUUID(),clock)!=null)ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.action(ANIMATIONS,"stopped","reason="+reason);
        if (client.player != null && PAIRS.containsKey(client.player.getUUID())) {
            clearPair(client.player.getUUID(), clock);
        }
        if (client.player != null) TIMELINE.stop(client.player.getUUID(), clock);
        if (notify) PlaybackClient.stop(reason);
        sharing = false;
        if (client.player == null || TIMELINE.pose(client.player.getUUID(), clock) == null) restoreCamera(client);
    }

    private static void camera(Minecraft client) {
        if (ToolVisualsClient.CONFIG.flag(EMOTE_THIRD_PERSON)) ru.he3hauka.toolvisuals.client.camera.ClientCamera.emote(client);
    }
    private static void restoreCamera(Minecraft client) {
        ru.he3hauka.toolvisuals.client.camera.ClientCamera.release(client, ru.he3hauka.toolvisuals.client.camera.CameraLeases.Owner.EMOTE);
    }
    public static void resetPlayback() {
        var client = Minecraft.getInstance();
        TIMELINE.clear(); STAGING.clear(); PAIRS.clear(); ANCHORS.clear(); sharing = false;
        restoreCamera(client);
    }

    public static void tick(Minecraft client) {
        PlaybackClient.tick(client);
        ru.he3hauka.toolvisuals.client.camera.ClientCamera.update(client);
        double clock = time(client);
        boolean enabled = ToolVisualsClient.CONFIG.active(ANIMATIONS);
        boolean synchronize=ToolVisualsClient.CONFIG.flag(EMOTE_SYNC);
        if(synchronizedSetting&&!synchronize){
            PlaybackClient.unsynchronize();sharing=false;
            if(client.player!=null)clearPair(client.player.getUUID(),clock);
        }
        synchronizedSetting=synchronize;
        boolean usable = enabled && client.player != null && client.level != null && client.gui.overlay() == null && client.isWindowActive();
        while (ToolVisualsClient.emoteKey.consumeClick()) if (usable && client.gui.screen() == null) {
            KeyMapping.releaseAll();
            client.gui.setScreen(new EmoteWheelScreen(null));
        }
        if (!enabled || client.player == null || client.level == null) {
            stop(client, clock, true,StopReason.DISABLED);
            TIMELINE.clear();
            STAGING.clear();
            PAIRS.clear();
            restoreCamera(client);
            return;
        }
        var local = TIMELINE.current(client.player.getUUID(), clock);
        if (local != null) {
            double dx = client.player.getX() - startX, dy = client.player.getY() - startY, dz = client.player.getZ() - startZ;
            if (!client.player.isAlive() || client.player.hurtTime > 0 || client.player.isSwinging() || client.player.isSleeping()
                    || client.player.isPassenger() || client.player.isFallFlying()
                    || (ToolVisualsClient.CONFIG.flag(EMOTE_CANCEL_MOVING) || PAIRS.containsKey(client.player.getUUID())) && dx * dx + dy * dy + dz * dz > .012) stop(client, clock, true,StopReason.INTERRUPTED);
        } else if (sharing || PAIRS.containsKey(client.player.getUUID())) stop(client, clock, true,StopReason.COMPLETED);
        TIMELINE.expire(clock);
        STAGING.expire(clock);
        PAIRS.entrySet().removeIf(entry -> TIMELINE.current(entry.getKey(), clock) == null);
        ANCHORS.keySet().removeIf(id -> PAIRS.values().stream().noneMatch(pair -> pair.id().equals(id)));
        if (TIMELINE.pose(client.player.getUUID(), clock) == null) restoreCamera(client);
    }

    public static EmotePose pose(UUID player) {
        if(ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active())return null;
        var client = Minecraft.getInstance();
        if (client.level == null || client.player == null || !ToolVisualsClient.CONFIG.active(ANIMATIONS)) return null;
        if (!player.equals(client.player.getUUID()) && !ToolVisualsClient.CONFIG.flag(EMOTE_OTHERS)) return null;
        return TIMELINE.pose(player, time(client));
    }

    public static void startPair(SocialProtocol.Started state) { startPair(state, state.age()); }
    private static void startPair(SocialProtocol.Started state, double age) {
        var client = Minecraft.getInstance();
        double clock = time(client);
        if (client.player == null || client.level == null || !ToolVisualsClient.CONFIG.active(ANIMATIONS)) return;
        if ((state.first().equals(client.player.getUUID()) || state.second().equals(client.player.getUUID())) && !ToolVisualsClient.CONFIG.flag(EMOTE_SYNC)) { SocialClient.cancel(); return; }
        var first = client.level.getPlayerByUUID(state.first());
        var second = client.level.getPlayerByUUID(state.second());
        if (first == null || second == null) return;
        boolean existing = PAIRS.values().stream().anyMatch(pair -> pair.id().equals(state.id()));
        if(!existing&&ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.action(ANIMATIONS,"pair_started","emote="+state.emote().name());
        if (!existing) {
            clearPair(state.first(), clock);
            clearPair(state.second(), clock);
        }
        var stage = ANCHORS.computeIfAbsent(state.id(), id -> PairStage.between(state.emote(), first.position(), second.position(), first.getYRot()));
        for (var id : java.util.List.of(state.first(), state.second())) {
            var previous = PAIRS.get(id);
            if (previous != null && previous.id().equals(state.id()) && TIMELINE.current(id, clock) != null) continue;
            PAIRS.put(id, state);
            TIMELINE.startPair(id, state.emote(), id.equals(state.second()), clock, age);
            if (stage != null) {
                boolean secondRole = id.equals(state.second());
                var entity = secondRole ? second : first;
                STAGING.start(id, entity.position(), entity.yBodyRot, secondRole ? stage.second : stage.first,
                        secondRole ? stage.secondYaw : stage.firstYaw, clock, state.emote().animation.duration - age);
            }
        }
        if (state.first().equals(client.player.getUUID()) || state.second().equals(client.player.getUUID())) {
            sharing = false;
            startX = client.player.getX(); startY = client.player.getY(); startZ = client.player.getZ();
            camera(client);
        }
    }
    private static void clearPair(UUID actor, double clock) {
        var pair = PAIRS.get(actor);
        if (pair == null) return;
        ANCHORS.remove(pair.id());
        for (var id : java.util.List.of(pair.first(), pair.second())) {
            if (PAIRS.get(id) != null && PAIRS.get(id).id().equals(pair.id())) { PAIRS.remove(id); TIMELINE.stop(id, clock); STAGING.stop(id, clock); }
        }
    }
    public static void stopPair(UUID nonce) {
        double clock = time(Minecraft.getInstance());
        for (var entry : java.util.List.copyOf(PAIRS.entrySet())) if (entry.getValue().id().equals(nonce)) clearPair(entry.getKey(), clock);
    }
    public static PairStagingTimeline.Frame staging(UUID actor, net.minecraft.world.phys.Vec3 actual, float originalYaw) {
        if(ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active())return null;
        var client=Minecraft.getInstance();
        if(!ToolVisualsClient.CONFIG.active(ANIMATIONS)||client.player==null||!actor.equals(client.player.getUUID())&&!ToolVisualsClient.CONFIG.flag(EMOTE_OTHERS))return null;
        return STAGING.frame(actor, actual, originalYaw, time(client));
    }

    public static boolean receive(ru.he3hauka.toolvisuals.animation.protocol.PlaybackWire.State state) {
        var client = Minecraft.getInstance();
        double clock = time(client);
        if(state.mode()==ru.he3hauka.toolvisuals.animation.protocol.PlaybackWire.Mode.STOPPED){
            clearPair(state.actor(),clock);TIMELINE.stop(state.actor(),clock);
            if(client.player!=null&&state.actor().equals(client.player.getUUID()))sharing=false;
            return true;
        }
        if (client.player == null || client.level == null || !ToolVisualsClient.CONFIG.active(ANIMATIONS)) return false;
        boolean self = state.actor().equals(client.player.getUUID());
        if (state.mode() == ru.he3hauka.toolvisuals.animation.protocol.PlaybackWire.Mode.PAIR) {
            var pair = state.pair();
            if (client.level.getPlayerByUUID(state.actor()) == null || client.level.getPlayerByUUID(pair.partner()) == null) return false;
            var first = pair.second() ? pair.partner() : state.actor();
            var second = pair.second() ? state.actor() : pair.partner();
            startPair(new SocialProtocol.Started(pair.id(), first, second, ru.he3hauka.toolvisuals.animation.PairEmote.byId(state.emote()), state.ageMillis() / 50), state.ageMillis() / 50d);
        } else if (!self) {
            if (client.level.getPlayerByUUID(state.actor()) == null) return false;
            clearPair(state.actor(), clock);
            TIMELINE.start(state.actor(), Emote.byId(state.emote()), clock, state.ageMillis() / 50d);
        }
        return true;
    }

    private static void receive(EmoteStatePayload payload) {
        var client = Minecraft.getInstance();
        double clock = time(client);
        if (PlaybackClient.supported() || client.level == null || client.player == null || payload.player().equals(client.player.getUUID())) return;
        if (payload.emote() == -1 && PAIRS.containsKey(payload.player())) return;
        clearPair(payload.player(), clock);
        if (payload.emote() == -1) {
            TIMELINE.stop(payload.player(), clock);
            if (payload.player().equals(client.player.getUUID())) sharing = false;
            return;
        }
        if(!ToolVisualsClient.CONFIG.active(ANIMATIONS)||client.level.getPlayerByUUID(payload.player())==null)return;
        var emote = Emote.byId(payload.emote());
        if (emote == null || payload.age() < 0 || payload.age() >= emote.duration) return;
        boolean self = payload.player().equals(client.player.getUUID());
        var current = TIMELINE.current(payload.player(), clock);
        if (self && current != null && current.emote() == emote) return;
        TIMELINE.start(payload.player(), emote, clock, payload.age());
    }
}
