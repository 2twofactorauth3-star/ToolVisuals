package ru.he3hauka.toolvisuals.client.presents;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.chat.message.ChatMessages;
import ru.he3hauka.toolvisuals.client.presents.network.PresentWorld;
import ru.he3hauka.toolvisuals.client.pvp.CombatCommands;
import ru.he3hauka.toolvisuals.client.pvp.PvPSave;
import ru.he3hauka.toolvisuals.client.waypoint.Waypoints;
import ru.he3hauka.toolvisuals.network.world.WorldContextWire.Area;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.TOOL_PRESENTS;

public final class ToolPresents {
    private static final PresentProgress PROGRESS = new PresentProgress(FabricLoader.getInstance().getConfigDir().resolve("toolvisuals-presents.json"));
    private static final PresentJourney JOURNEY = new PresentJourney();
    private static final ExecutorService ROUTER = Executors.newSingleThreadExecutor(task -> {
        var thread = new Thread(task, "ToolVisuals present route"); thread.setDaemon(true); return thread;
    });
    private static List<Present> route = List.of();
    private static CompletableFuture<List<Present>> planning;
    private static String context = "";
    private static Area area = Area.OTHER;
    private static ClientLevel level;
    private static Vec3 origin;
    private static int revision, plannedRevision, ticks;

    private ToolPresents() {}

    public static void initialize() {
        PresentWorld.initialize();
        try { PROGRESS.load(); }
        catch (IOException error) { org.slf4j.LoggerFactory.getLogger("ToolVisuals").warn("Cannot load presents; original progress preserved", error); }
    }

    public static boolean active() {
        var client = Minecraft.getInstance();
        return !context(client).isEmpty() && PresentWorld.area(client) != Area.OTHER;
    }

    public static List<Present> route() {
        var client = Minecraft.getInstance();
        return active() && context(client).equals(context) && area == PresentWorld.area(client) && level == client.level ? route : List.of();
    }
    public static int collected() { return context.isEmpty() ? 0 : PROGRESS.collected(context).size(); }
    public static boolean spawnComplete() { return !context.isEmpty() && PresentCatalog.spawnComplete(PROGRESS.collected(context)); }
    public static Area area() { return PresentWorld.area(Minecraft.getInstance()); }

    private static String accountContext(Minecraft client) {
        return client.level != null && client.player != null && !Waypoints.context().isEmpty()
                ? Waypoints.context() + "/" + client.player.getUUID() : "";
    }

    private static String context(Minecraft client) {
        return ToolVisualsClient.CONFIG.active(TOOL_PRESENTS)
                && !ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active() ? accountContext(client) : "";
    }

    public static void tick(Minecraft client) {
        String current = context(client);
        Area currentArea = current.isEmpty() ? Area.OTHER : PresentWorld.area(client);
        if (!current.equals(context) || area != currentArea || level != client.level) {
            context = current; area = currentArea; level = client.level;
            route = List.of(); origin = null; revision++;
        }
        if (!ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active())
            JOURNEY.scope(client.getConnection(), accountContext(client));
        if (planning != null && planning.isDone()) {
            if (plannedRevision == revision && !context.isEmpty()) {
                try { route = planning.join(); }
                catch (RuntimeException error) { org.slf4j.LoggerFactory.getLogger("ToolVisuals").warn("Cannot calculate present route", error); }
            }
            planning = null;
        }
        if (context.isEmpty()) return;
        JOURNEY.travel(area, PROGRESS.collected(context), () -> sendArenaCommand(client));
        if (area == Area.OTHER || planning != null) return;
        if (origin != null && (++ticks < 100 || client.player.position().distanceToSqr(origin) < 16 * 16)) return;
        origin = client.player.position(); ticks = 0;
        var remaining = PresentCatalog.remaining(area, PROGRESS.collected(context));
        if (remaining.isEmpty()) { route = List.of(); return; }
        var start = origin;
        plannedRevision = revision;
        planning = CompletableFuture.supplyAsync(() -> PresentRoute.plan(start.x, start.y, start.z, remaining), ROUTER);
    }

    private static boolean sendArenaCommand(Minecraft client) {
        if (client.getConnection() == null || client.player == null || !client.player.isAlive() || client.player.isSpectator()) return false;
        if (PvPSave.remaining() > 0 && CombatCommands.blocks(PresentJourney.ARENA_COMMAND, ToolVisualsClient.CONFIG.pvpBlockedCommands())) return false;
        client.getConnection().sendCommand(PresentJourney.ARENA_COMMAND);
        ChatMessages.notice("presents.arena_warp");
        return true;
    }

    public static void clicked(BlockPos position) {
        var client = Minecraft.getInstance();
        if (!active() || !context(client).equals(context) || client.player.isSpectator()
                || !client.level.getWorldBorder().isWithinBounds(position) || !client.player.isWithinBlockInteractionRange(position, 0)) return;
        var present = PresentCatalog.at(PresentWorld.area(client), position.getX(), position.getY(), position.getZ());
        if (present == null) return;
        try {
            if (!PROGRESS.collect(context, present)) return;
            route = route.stream().filter(point -> point.id() != present.id()).toList();
            origin = null; revision++;
            ChatMessages.notice("presents.collected", present.id(), collected(), PresentCatalog.ALL.size());
        } catch (IOException | IllegalArgumentException error) {
            ChatMessages.notice("presents.save_error");
        }
    }

    public static void reset() {
        if (!active() || context.isEmpty()) { ChatMessages.notice("presents.spawn_only"); return; }
        try {
            PROGRESS.reset(context); origin = null; revision++; route = List.of();
            JOURNEY.scope(null, "");
            ChatMessages.notice("presents.reset_done");
        } catch (IOException error) { ChatMessages.notice("presents.save_error"); }
    }

    public static void recalculate() { origin = null; revision++; }
}
