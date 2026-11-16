package ru.he3hauka.toolvisuals.client.connection.policy;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.screens.Screen;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.animation.ui.EmoteWheelScreen;
import ru.he3hauka.toolvisuals.client.armor.ui.ArmorSettingsScreen;
import ru.he3hauka.toolvisuals.client.auction.ui.AuctionScreen;
import ru.he3hauka.toolvisuals.client.cosmetic.accessory.ui.CosmeticsScreen;
import ru.he3hauka.toolvisuals.client.cosmetic.outfit.ui.OutfitScreen;
import ru.he3hauka.toolvisuals.client.crosshair.ui.CrosshairEditorScreen;
import ru.he3hauka.toolvisuals.client.friends.ui.FriendsScreen;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsScreen;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.hand.ui.HandEditorScreen;
import ru.he3hauka.toolvisuals.client.hand.ui.HandSettingsScreen;
import ru.he3hauka.toolvisuals.client.hud.layout.HudEditorScreen;
import ru.he3hauka.toolvisuals.client.items.highlight.ui.ItemHighlightAppearanceScreen;
import ru.he3hauka.toolvisuals.client.items.highlight.ui.ItemHighlightsScreen;
import ru.he3hauka.toolvisuals.client.keybinds.ui.KeybindEditorScreen;
import ru.he3hauka.toolvisuals.client.keybinds.ui.KeybindsScreen;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.music.ui.MusicScreen;
import ru.he3hauka.toolvisuals.client.navigation.gps.ui.GpsScreen;
import ru.he3hauka.toolvisuals.client.pvp.ui.PvPCommandsScreen;
import ru.he3hauka.toolvisuals.client.render.vignette.ui.DamageVignetteScreen;
import ru.he3hauka.toolvisuals.client.replay.ui.ReplayLibraryScreen;
import ru.he3hauka.toolvisuals.client.replay.ui.ReplayViewerScreen;
import ru.he3hauka.toolvisuals.client.social.presence.ui.ClientBrowserScreen;
import ru.he3hauka.toolvisuals.client.social.profile.PlayerCardScreen;
import ru.he3hauka.toolvisuals.client.social.ui.PairedEmoteScreen;
import ru.he3hauka.toolvisuals.client.sound.ui.SoundLibraryScreen;
import ru.he3hauka.toolvisuals.client.utility.chat.ui.ChatToolsScreen;
import ru.he3hauka.toolvisuals.client.voice.ui.VoiceChatScreen;
import ru.he3hauka.toolvisuals.client.waypoint.ui.WaypointsScreen;

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
            if (screen instanceof OutfitScreen
                    && !ToolVisualsClient.CONFIG.available(ClientFeature.OUTFITS)) return true;
            if (screen instanceof ArmorSettingsScreen armor
                    && !ToolVisualsClient.CONFIG.available(armor.status ? VisualModule.ARMOR_COLOR : VisualModule.ARMOR_HUD)) return true;
            if (screen instanceof ChatToolsScreen chat
                    && !ToolVisualsClient.CONFIG.available(chat.privacy ? VisualModule.STREAMER_MODE : VisualModule.CHAT_TABS)) return true;
            if (screen instanceof VisualsScreen visuals && !visuals.settingsAvailable()) return true;
            if (!(screen instanceof VisualEditorScreen editor)) break;
            screen = editor.parent;
        }
        return false;
    }

    private static Map<Class<? extends Screen>, String> owners() {
        var result = new HashMap<Class<? extends Screen>, String>();
        add(result, VisualModule.AUCTION_HELPER.id, AuctionScreen.class);
        add(result, VisualModule.REPLAY.id, ReplayLibraryScreen.class,
                ReplayViewerScreen.class);
        add(result, VisualModule.KEYBINDS.id, KeybindsScreen.class,
                KeybindEditorScreen.class);
        add(result, VisualModule.CUSTOM_HAND.id, HandSettingsScreen.class,
                HandEditorScreen.class);
        add(result, VisualModule.CROSSHAIR.id, CrosshairEditorScreen.class);
        add(result, VisualModule.SOUND_CUSTOMIZER.id, SoundLibraryScreen.class);
        add(result, VisualModule.GPS.id, GpsScreen.class);
        add(result, VisualModule.ITEM_HIGHLIGHTS.id, ItemHighlightsScreen.class,
                ItemHighlightAppearanceScreen.class);
        add(result, VisualModule.WAYPOINTS.id, WaypointsScreen.class);
        add(result, VisualModule.DAMAGE_VIGNETTE.id, DamageVignetteScreen.class);
        add(result, VisualModule.PVP_SAVE.id, PvPCommandsScreen.class);
        add(result, VisualModule.VOICE_CHAT.id, VoiceChatScreen.class);
        add(result, VisualModule.MUSIC_HUD.id, MusicScreen.class);
        add(result, VisualModule.ANIMATIONS.id, EmoteWheelScreen.class,
                PairedEmoteScreen.class);
        add(result, VisualModule.CLIENT_LIST.id, ClientBrowserScreen.class,
                PlayerCardScreen.class);
        add(result, VisualModule.COSMETICS.id, CosmeticsScreen.class);
        add(result, ClientFeature.FRIENDS.id, FriendsScreen.class);
        add(result, ClientFeature.HUD_EDITOR.id, HudEditorScreen.class);
        add(result, ClientFeature.OUTFITS.id, OutfitScreen.class);
        return Map.copyOf(result);
    }

    @SafeVarargs
    private static void add(Map<Class<? extends Screen>, String> result, String id, Class<? extends Screen>... screens) {
        for (var screen : screens) {
            if (result.putIfAbsent(screen, id) != null) throw new IllegalStateException("Duplicate feature screen: " + screen);
        }
    }
}
