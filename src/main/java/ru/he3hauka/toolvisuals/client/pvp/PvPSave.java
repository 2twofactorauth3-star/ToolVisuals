package ru.he3hauka.toolvisuals.client.pvp;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.command.CommandFix;
import ru.he3hauka.toolvisuals.client.player.ClientActions;
import ru.he3hauka.toolvisuals.client.player.FakePlayer;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.PVP_SAVE;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.PVP_SAVE_CONFIRMED;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.PVP_SAVE_DURATION;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.PVP_SAVE_RESET_ON_DEATH;

public final class PvPSave {
    private static final CombatTimer TIMER = new CombatTimer();
    private static Object connection;
    private static long lastNotice;
    private static String lastCommand = "";
    private PvPSave() {}
    public static void tick(Minecraft client) {
        if (connection != client.getConnection() || client.level == null || !ToolVisualsClient.CONFIG.active(PVP_SAVE) || resetAfterDeath(client)) TIMER.clear();
        connection = client.getConnection();
    }
    private static void begin() {
        if (resetAfterDeath(Minecraft.getInstance())) { TIMER.clear(); return; }
        if (ToolVisualsClient.CONFIG.active(PVP_SAVE)) TIMER.begin(System.nanoTime(),(int)ToolVisualsClient.CONFIG.value(PVP_SAVE_DURATION));
    }
    private static boolean resetAfterDeath(Minecraft client) {
        return ToolVisualsClient.CONFIG.flag(PVP_SAVE_RESET_ON_DEATH) && client.player != null && client.player.isDeadOrDying();
    }
    public static void attacked(Entity target) {
         
        if (ToolVisualsClient.CONFIG.flag(PVP_SAVE_CONFIRMED)) return;
        if (target instanceof Player && !FakePlayer.owns(target) && target != Minecraft.getInstance().player) begin();
    }
    public static void damage(ClientboundDamageEventPacket packet) {
        var client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;
        Entity victim = client.level.getEntity(packet.entityId()), attacker = packet.getSource(client.level).getEntity();
        if (victim instanceof Player && attacker instanceof Player && victim != attacker && !FakePlayer.owns(victim) && !FakePlayer.owns(attacker)
                && (victim == client.player || attacker == client.player)) begin();
    }
    public static int remaining() {
        if (resetAfterDeath(Minecraft.getInstance())) TIMER.clear();
        return ToolVisualsClient.CONFIG.active(PVP_SAVE) ? TIMER.remaining(System.nanoTime()) : 0;
    }
    public static boolean allowCommand(String command) {
        int seconds = remaining();
        if (seconds == 0) return true;
        String corrected = CommandFix.outgoing(command);
        if (!CombatCommands.blocks(corrected,ToolVisualsClient.CONFIG.pvpBlockedCommands())) return true;
        long now = System.nanoTime();
        if (!corrected.equals(lastCommand) || now - lastNotice >= 1_000_000_000L) {
            lastCommand = corrected; lastNotice = now;
            ClientActions.notice("pvp_save.command_blocked",seconds);
        }
        return false;
    }
}
