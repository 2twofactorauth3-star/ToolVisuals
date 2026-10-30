package ru.he3hauka.toolvisuals.client.player;

import net.minecraft.client.Minecraft;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.chat.message.ChatMessages;
import ru.he3hauka.toolvisuals.client.friends.Friends;
import ru.he3hauka.toolvisuals.client.items.ChangeHand;
import ru.he3hauka.toolvisuals.client.items.ElytraSwap;
import ru.he3hauka.toolvisuals.client.pvp.PvPSave;
import ru.he3hauka.toolvisuals.client.pvp.TotemCounter;
import ru.he3hauka.toolvisuals.client.sound.SoundCustomizer;
import ru.he3hauka.toolvisuals.client.waypoint.Waypoints;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.FAKE_PLAYER;

public final class ClientActions {
    private ClientActions() {}
    public static void tick(Minecraft client) {
        FakePlayer.tick(client); ElytraSwap.tick(client); PvPSave.tick(client); Friends.tick(client);
        Waypoints.tick(client);
        ru.he3hauka.toolvisuals.client.navigation.gps.Gps.tick(client);
        SoundCustomizer.tick(client);
        TotemCounter.tick(client);
        ru.he3hauka.toolvisuals.client.hud.animation.BetterHud.tick(client);
        boolean usable = client.player != null && client.level != null && client.gui.screen() == null
                && client.gui.overlay() == null && client.isWindowActive() && !client.isPaused();
        while (ToolVisualsClient.changeHandKey.consumeClick()) if (usable) ChangeHand.key(client);
        while (ToolVisualsClient.elytraSwapKey.consumeClick()) if (usable) ElytraSwap.request(client);
        while (ToolVisualsClient.fakePlayerKey.consumeClick()) if (usable && ToolVisualsClient.CONFIG.active(FAKE_PLAYER)) FakePlayer.toggle(client);
    }
    public static void notice(String key,Object... args) {
        SoundCustomizer.notice();
        ChatMessages.notice(key,args);
    }
}
