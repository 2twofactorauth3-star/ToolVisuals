package ru.he3hauka.toolvisuals.client.minigame;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.chat.message.ChatMessages;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;
import ru.he3hauka.toolvisuals.client.minigame.network.MiniGamePayload;
import ru.he3hauka.toolvisuals.client.minigame.ui.MiniGameLibraryScreen;
import ru.he3hauka.toolvisuals.client.minigame.ui.MiniGamePlayScreen;
import ru.he3hauka.toolvisuals.minigame.GameKind;
import ru.he3hauka.toolvisuals.minigame.GameRound;
import ru.he3hauka.toolvisuals.minigame.session.GameReplica;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.MINI_GAMES;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.MINIGAMES_INVITATIONS;
import static ru.he3hauka.toolvisuals.minigame.network.MiniGameWire.*;

public final class MiniGamesClient {
    private static Object connection, life, world;
    private static GameReplica replica;
    private static GameRound solo, animated;
    private static CompletableFuture<int[]> computer;
    private static long animationStart, nextHeartbeat;
    private static boolean openRequested;

    private MiniGamesClient() {}

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(MiniGamePayload.Request.TYPE, MiniGamePayload.Request.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(MiniGamePayload.State.TYPE, MiniGamePayload.State.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(MiniGamePayload.State.TYPE, (payload, context) -> receive(context.client(), payload.value()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> MiniGameInvitation.register(dispatcher,
                () -> ToolVisualsClient.CONFIG.available(MINI_GAMES)));
        ClientTickEvents.END_CLIENT_TICK.register(MiniGamesClient::tick);
    }

    private static void reset() { connection = life = world = null; replica = null; solo = animated = null; computer = null; openRequested = false; nextHeartbeat = 0; }

    private static void scope(Minecraft client) {
        if (connection != client.getConnection()) { reset(); connection = client.getConnection(); }
        if (client.player != null && replica == null) replica = new GameReplica(UUID.randomUUID(), client.player.getUUID());
        if (life != client.player || world != client.level) {
            if (life != null) { if (replica != null) replica.leave(System.nanoTime()); solo = animated = null; computer = null; }
            life = client.player; world = client.level;
        }
    }

    public static boolean supported() { return Minecraft.getInstance().getConnection() != null && ClientPlayNetworking.canSend(MiniGamePayload.Request.TYPE); }
    public static boolean ready() { return ToolVisualsClient.CONFIG.active(MINI_GAMES) && supported() && replica != null && replica.ready(); }
    public static boolean busy() { return replica != null && replica.busy(); }
    public static Message room() { return replica == null ? null : replica.room(); }
    public static GameRound round() { return solo != null ? solo : replica == null ? null : replica.round(); }
    public static boolean solo() { return solo != null; }
    public static int ownTurn() { return solo != null ? 1 : replica == null ? 1 : replica.ownTurn(); }
    public static boolean canPlay() { var round = round(); return ToolVisualsClient.CONFIG.active(MINI_GAMES) && round != null && round.winner == 0 && round.turn == ownTurn() && (solo != null || ready() && !busy()) && !animating(); }

    public static void requestOpen() { openRequested = true; }
    public static void open(Screen parent) { Minecraft.getInstance().gui.setScreen(new MiniGameLibraryScreen(parent)); }

    public static void startSolo(GameKind kind) {
        if(!ToolVisualsClient.CONFIG.available(MINI_GAMES))return;
        ToolVisualsClient.CONFIG.setActive(MINI_GAMES, true); stop();
        solo = new GameRound(kind, UUID.randomUUID().getLeastSignificantBits()); observe(solo);
        Minecraft.getInstance().gui.setScreen(new MiniGamePlayScreen(null));
    }

    public static boolean invite(UUID player, GameKind kind) {
        return ready() && replica != null && solo == null && replica.invite(player, kind, System.nanoTime());
    }

    public static boolean answer(UUID invitation, boolean accept) {
        if (!ToolVisualsClient.CONFIG.active(MINI_GAMES) || replica == null || !ToolVisualsClient.CONFIG.flag(MINIGAMES_INVITATIONS) && accept) return false;
        boolean answered = replica.answer(invitation, accept, System.nanoTime());
        if (answered && accept) { solo = null; computer = null; }
        return answered;
    }

    public static void move(int first, int second) {
        if (!canPlay()) return;
        if (solo != null) { solo = solo.play(first, second); observe(solo); }
        else replica.move(first, second, System.nanoTime());
    }

    public static void stop() {
        solo = animated = null; computer = null;
        if (replica != null && supported()) replica.leave(System.nanoTime());
    }

    public static double animationTime() { return Math.max(0, (System.nanoTime() - animationStart) / 1_000_000_000d * 1.6); }
    public static boolean animating() { var round = round(); return round != null && round.kind == GameKind.ANGRY_BIRDS && round.shot != null && animationTime() < round.shot.duration() + .3; }
    private static void observe(GameRound next) { if (animated != next) { animated = next; animationStart = System.nanoTime(); } }

    private static void receive(Minecraft client, Message message) {
        scope(client); if (replica == null) return;
        if (!ToolVisualsClient.CONFIG.active(MINI_GAMES) && replica.enabled()) replica.enable(false, System.nanoTime());
        var previous = replica.round();
        var previousRoom = replica.room();
        boolean changed;
        try { changed = replica.receive(message, System.nanoTime(), ToolVisualsClient.CONFIG.flag(MINIGAMES_INVITATIONS)); }
        catch (IllegalArgumentException invalid) { ClientErrors.report("Read synchronized minigame state", invalid); replica.leave(System.nanoTime()); return; }
        if (!changed) return;
        if (message.code() == Code.INVITED) ChatMessages.send(MiniGameInvitation.message(message, ru.he3hauka.toolvisuals.client.social.SocialClient.name(message.first())));
        else if (message.code() == Code.MATCH && replica.round() != null) {
            solo = null; computer = null; observe(replica.round());
            if (previous == null) client.gui.setScreen(new MiniGamePlayScreen(null));
        } else if (message.code() != Code.WAITING && !(message.code() == Code.CLOSED && previousRoom == null && message.room().equals(NONE)))
            ChatMessages.notice("minigames.result." + message.code().name().toLowerCase(java.util.Locale.ROOT));
    }

    private static void tick(Minecraft client) {
        scope(client); if (client.player == null || client.level == null) return;
        long now = System.nanoTime(); boolean enabled = ToolVisualsClient.CONFIG.active(MINI_GAMES);
        if (!enabled) {
            solo = animated = null; computer = null;
            if (client.gui.screen() instanceof MiniGamePlayScreen) client.gui.setScreen(null);
        }
        if (supported() && replica != null) {
            if (replica.enabled() != enabled) { replica.enable(enabled, now); nextHeartbeat = now + 20_000_000_000L; }
            replica.expireInvitation(now);
            if (!ToolVisualsClient.CONFIG.flag(MINIGAMES_INVITATIONS) && replica.invitation() != null && !replica.busy()) replica.answer(replica.invitation().room(), false, now);
            if (replica.timedOut(now) && enabled) { ChatMessages.notice("minigames.timeout"); replica.leave(now); nextHeartbeat = now + 1_000_000_000L; }
            if (enabled && now >= nextHeartbeat && !replica.busy()) { replica.heartbeat(now); nextHeartbeat = now + (replica.room() == null ? 20_000_000_000L : 5_000_000_000L); }
            var command = replica.due(now);
            if (command != null) try { ClientPlayNetworking.send(new MiniGamePayload.Request(command)); }
            catch (RuntimeException error) { ClientErrors.report("Send minigame request", error); }
        }
        if (openRequested) { openRequested = false; open(client.gui.screen()); }
        if (solo != null && solo.winner == 0 && solo.turn == 2 && computer == null && !animating()) {
            GameRound expected = solo;
            computer = CompletableFuture.supplyAsync(expected::computerMove).handle((move, error) -> {
                if (error != null) ClientErrors.report("Choose minigame computer move", error); return move;
            });
        }
        if (computer != null && computer.isDone()) {
            int[] move = computer.getNow(null); computer = null;
            if (solo != null && solo.winner == 0 && solo.turn == 2 && move != null) { solo = solo.play(move[0], move[1]); observe(solo); }
            else if (move == null) solo = null;
        }
    }
}
