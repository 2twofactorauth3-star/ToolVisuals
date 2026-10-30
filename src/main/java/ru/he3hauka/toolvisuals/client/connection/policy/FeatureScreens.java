package ru.he3hauka.toolvisuals.client.connection.policy;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.gui.screens.Screen;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsScreen;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.module.VisualModule;

public final class FeatureScreens {
    private static final Map<Class<? extends Screen>, String> OWNERS = owners();
    private static final Map<String, String> PACKAGES = Map.of(
            "ru.he3hauka.toolvisuals.client.cape.ui", VisualModule.CAPES.id,
            "ru.he3hauka.toolvisuals.client.minigame.ui", VisualModule.MINI_GAMES.id);

    private FeatureScreens() {}

    public static boolean blocked(Screen screen) {
        for (int depth = 0; screen != null && depth < 32; depth++) {
            String owner = OWNERS.get(screen.getClass());
            if (owner == null) owner = PACKAGES.get(screen.getClass().getPackageName());
            if (owner != null && !ToolVisualsClient.CONFIG.serverPolicy.allows(owner)) return true;
            if (screen instanceof ru.he3hauka.toolvisuals.client.cosmetic.outfit.ui.OutfitScreen
                    && !ToolVisualsClient.CONFIG.available(ClientFeature.OUTFITS)) return true;
            if (screen instanceof ru.he3hauka.toolvisuals.client.armor.ui.ArmorSettingsScreen armor
                    && !ToolVisualsClient.CONFIG.available(armor.status ? VisualModule.ARMOR_COLOR : VisualModule.ARMOR_HUD)) return true;
            if (screen instanceof ru.he3hauka.toolvisuals.client.utility.chat.ui.ChatToolsScreen chat
                    && !ToolVisualsClient.CONFIG.available(chat.privacy ? VisualModule.STREAMER_MODE : VisualModule.CHAT_TABS)) return true;
            if (screen instanceof VisualsScreen visuals && !visuals.settingsAvailable()) return true;
            if (!(screen instanceof VisualEditorScreen editor)) break;
            screen = editor.parent;
        }
        return false;
    }

    private static Map<Class<? extends Screen>, String> owners() {
        var result = new HashMap<Class<? extends Screen>, String>();
        add(result, VisualModule.AUCTION_HELPER.id, ru.he3hauka.toolvisuals.client.auction.ui.AuctionScreen.class);
        add(result, VisualModule.REPLAY.id, ru.he3hauka.toolvisuals.client.replay.ui.ReplayLibraryScreen.class,
                ru.he3hauka.toolvisuals.client.replay.ui.ReplayViewerScreen.class);
        add(result, VisualModule.KEYBINDS.id, ru.he3hauka.toolvisuals.client.keybinds.ui.KeybindsScreen.class,
                ru.he3hauka.toolvisuals.client.keybinds.ui.KeybindEditorScreen.class);
        add(result, VisualModule.CUSTOM_HAND.id, ru.he3hauka.toolvisuals.client.hand.ui.HandSettingsScreen.class,
                ru.he3hauka.toolvisuals.client.hand.ui.HandEditorScreen.class);
        add(result, VisualModule.CROSSHAIR.id, ru.he3hauka.toolvisuals.client.crosshair.ui.CrosshairEditorScreen.class);
        add(result, VisualModule.WAYPOINTS.id, ru.he3hauka.toolvisuals.client.waypoint.ui.WaypointsScreen.class);
        add(result, VisualModule.DAMAGE_VIGNETTE.id, ru.he3hauka.toolvisuals.client.render.vignette.ui.DamageVignetteScreen.class);
        add(result, VisualModule.PVP_SAVE.id, ru.he3hauka.toolvisuals.client.pvp.ui.PvPCommandsScreen.class);
        add(result, VisualModule.VOICE_CHAT.id, ru.he3hauka.toolvisuals.client.voice.ui.VoiceChatScreen.class);
        add(result, VisualModule.MUSIC_HUD.id, ru.he3hauka.toolvisuals.client.music.ui.MusicScreen.class);
        add(result, VisualModule.ANIMATIONS.id, ru.he3hauka.toolvisuals.client.animation.ui.EmoteWheelScreen.class,
                ru.he3hauka.toolvisuals.client.social.ui.PairedEmoteScreen.class);
        add(result, VisualModule.CLIENT_LIST.id, ru.he3hauka.toolvisuals.client.social.presence.ui.ClientBrowserScreen.class,
                ru.he3hauka.toolvisuals.client.social.profile.PlayerCardScreen.class);
        add(result, VisualModule.COSMETICS.id, ru.he3hauka.toolvisuals.client.cosmetic.accessory.ui.CosmeticsScreen.class);
        add(result, ClientFeature.FRIENDS.id, ru.he3hauka.toolvisuals.client.friends.ui.FriendsScreen.class);
        add(result, ClientFeature.HUD_EDITOR.id, ru.he3hauka.toolvisuals.client.hud.layout.HudEditorScreen.class);
        add(result, ClientFeature.OUTFITS.id, ru.he3hauka.toolvisuals.client.cosmetic.outfit.ui.OutfitScreen.class);
        return Map.copyOf(result);
    }

    @SafeVarargs
    private static void add(Map<Class<? extends Screen>, String> result, String id, Class<? extends Screen>... screens) {
        for (var screen : screens) {
            if (result.putIfAbsent(screen, id) != null) throw new IllegalStateException("Duplicate feature screen: " + screen);
        }
    }
}
