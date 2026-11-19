package ru.he3hauka.toolvisuals.client.chat.reply;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.player.Player;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.player.FakePlayer;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.AUTO_LOSE;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.AUTO_WIN;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.AUTO_LOSE_PRESET;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.AUTO_WIN_PRESET;

public final class AutoGG {
    private static final ReplyTracker TRACKER = new ReplyTracker();
    private static Object connection, world, player;
    private static int enabled;
    private AutoGG() {}
    private static boolean scope(Minecraft client) {
        var config = ToolVisualsClient.CONFIG;
        int mask = config.active(AUTO_WIN) ? 1 : 0;
        if (config.active(AUTO_LOSE)) mask |= 2;
        if (ReplayScope.active() || client.getConnection() == null || client.level == null || client.player == null) mask = 0;
        if (connection != client.getConnection() || world != client.level || player != client.player || enabled != mask) TRACKER.clear();
        connection = client.getConnection(); world = client.level; player = client.player; enabled = mask;
        return mask != 0;
    }
    public static void tick(Minecraft client) { scope(client); }
    public static void damage(ClientboundDamageEventPacket packet) {
        var client = Minecraft.getInstance();
        if (!scope(client) || !(client.level.getEntity(packet.entityId()) instanceof Player victim)) return;
        var attacker = packet.getSource(client.level).getEntity();
        if (!FakePlayer.owns(victim) && attacker instanceof Player source && !FakePlayer.owns(source)
                && (victim == client.player || source == client.player))
            TRACKER.damage(victim.getUUID(), source.getUUID(), source.getName().getString(), System.nanoTime());
        else TRACKER.forget(victim.getUUID());
    }
    public static void event(ClientboundEntityEventPacket packet) {
        var client = Minecraft.getInstance();
        if (scope(client) && packet.getEventId() == 3 && packet.getEntity(client.level) instanceof Player victim) death(victim);
    }
    public static void death(Player victim) {
        var client = Minecraft.getInstance();
        if (!scope(client) || victim.level() != client.level || FakePlayer.owns(victim) || victim.isSpectator()) return;
        var reply = TRACKER.death(victim.getUUID(), victim.getName().getString(), client.player.getUUID(), (enabled & 1) != 0, (enabled & 2) != 0, System.nanoTime());
        if (reply == null) return;
        var preset = reply.win ? AUTO_WIN_PRESET : AUTO_LOSE_PRESET;
        String text = ReplyPresets.message(reply.win, (int)ToolVisualsClient.CONFIG.value(preset), reply.player);
        if (text != null) client.getConnection().sendChat(text);
    }
}
