package ru.he3hauka.toolvisuals.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.io.IOException;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.LoggerFactory;
import ru.he3hauka.toolvisuals.ToolVisuals;
import ru.he3hauka.toolvisuals.client.animation.Emotes;
import ru.he3hauka.toolvisuals.client.animation.movement.ExtraAnimations;
import ru.he3hauka.toolvisuals.client.armor.render.ArmorRenderTypes;
import ru.he3hauka.toolvisuals.client.auction.AuctionHelper;
import ru.he3hauka.toolvisuals.client.auction.AuctionStore;
import ru.he3hauka.toolvisuals.client.camera.FreeLook;
import ru.he3hauka.toolvisuals.client.cape.CapeClient;
import ru.he3hauka.toolvisuals.client.cape.CapeStore;
import ru.he3hauka.toolvisuals.client.chat.ChatResizer;
import ru.he3hauka.toolvisuals.client.command.CommandFix;
import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.config.ConfigProfiles;
import ru.he3hauka.toolvisuals.client.connection.AccountStore;
import ru.he3hauka.toolvisuals.client.connection.ConnectionIdentity;
import ru.he3hauka.toolvisuals.client.connection.holyworld.HolyWorldClient;
import ru.he3hauka.toolvisuals.client.connection.ui.ProtocolUi;
import ru.he3hauka.toolvisuals.client.cosmetic.accessory.AccessoryClient;
import ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.AccessoryPipeline;
import ru.he3hauka.toolvisuals.client.cosmetic.outfit.OutfitStore;
import ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;
import ru.he3hauka.toolvisuals.client.discord.DiscordPresence;
import ru.he3hauka.toolvisuals.client.effects.WorldEffects;
import ru.he3hauka.toolvisuals.client.friends.FriendStore;
import ru.he3hauka.toolvisuals.client.friends.network.FriendDataClient;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsScreen;
import ru.he3hauka.toolvisuals.client.gui.render.UiFont;
import ru.he3hauka.toolvisuals.client.gui.render.UiPipelines;
import ru.he3hauka.toolvisuals.client.gui.text.FontWarmup;
import ru.he3hauka.toolvisuals.client.hud.ExtraHud;
import ru.he3hauka.toolvisuals.client.hud.health.HealthIndicator;
import ru.he3hauka.toolvisuals.client.hud.tab.ExtraTab;
import ru.he3hauka.toolvisuals.client.input.tape.TapeMouse;
import ru.he3hauka.toolvisuals.client.items.dropprotect.DropProtect;
import ru.he3hauka.toolvisuals.client.items.highlight.render.HandItemGlow;
import ru.he3hauka.toolvisuals.client.items.highlight.render.ItemGlowPipeline;
import ru.he3hauka.toolvisuals.client.items.shulkertooltip.ShulkerTooltips;
import ru.he3hauka.toolvisuals.client.keybinds.Keybinds;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.menu.multiplayer.MultiplayerVersionsButton;
import ru.he3hauka.toolvisuals.client.minigame.MiniGamesClient;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.music.MusicClient;
import ru.he3hauka.toolvisuals.client.nameprotect.NameProtect;
import ru.he3hauka.toolvisuals.client.navigation.gps.Gps;
import ru.he3hauka.toolvisuals.client.navigation.gps.GpsStore;
import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import ru.he3hauka.toolvisuals.client.optimization.crystal.CrystalOptimizer;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.report.RuntimeMetadata;
import ru.he3hauka.toolvisuals.client.optimization.frame.FrameMonitor;
import ru.he3hauka.toolvisuals.client.optimization.hud.PlayerRoster;
import ru.he3hauka.toolvisuals.client.optimization.occlusion.EntityCullingBridge;
import ru.he3hauka.toolvisuals.client.optimization.occlusion.OcclusionService;
import ru.he3hauka.toolvisuals.client.optimization.preset.StartupPreset;
import ru.he3hauka.toolvisuals.client.optimization.terrain.TerrainSettings;
import ru.he3hauka.toolvisuals.client.optimization.texture.TextureUploads;
import ru.he3hauka.toolvisuals.client.player.ClientActions;
import ru.he3hauka.toolvisuals.client.presents.ToolPresents;
import ru.he3hauka.toolvisuals.client.pvp.PvPSave;
import ru.he3hauka.toolvisuals.client.render.vignette.DamageVignette;
import ru.he3hauka.toolvisuals.client.replay.capture.ReplayCapture;
import ru.he3hauka.toolvisuals.client.replay.ui.ReplayLibraryScreen;
import ru.he3hauka.toolvisuals.client.replay.ui.ReplayViewerScreen;
import ru.he3hauka.toolvisuals.client.session.SessionStats;
import ru.he3hauka.toolvisuals.client.social.PairedEmoteInput;
import ru.he3hauka.toolvisuals.client.social.SocialClient;
import ru.he3hauka.toolvisuals.client.social.presence.ClientListClient;
import ru.he3hauka.toolvisuals.client.sound.library.CustomSounds;
import ru.he3hauka.toolvisuals.client.sound.library.SoundLibrary;
import ru.he3hauka.toolvisuals.client.utility.UtilityPreferences;
import ru.he3hauka.toolvisuals.client.utility.chat.ChatTabs;
import ru.he3hauka.toolvisuals.client.utility.durability.DurabilityAlert;
import ru.he3hauka.toolvisuals.client.utility.items.ItemInfo;
import ru.he3hauka.toolvisuals.client.utility.privacy.PrivacyMode;
import ru.he3hauka.toolvisuals.client.voice.BundledVoiceChat;
import ru.he3hauka.toolvisuals.client.waypoint.WaypointStore;
import ru.he3hauka.toolvisuals.mixin.render.RenderPipelinesAccessor;
import ru.he3hauka.toolvisuals.protocols.client.ProtocolMenus;

public final class ToolVisualsClient implements ClientModInitializer {
    public static final ClientConfig CONFIG = new ClientConfig(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals.json"));
    public static final AccountStore ACCOUNTS = new AccountStore(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals-accounts.json"));
    public static final ConfigProfiles PROFILES = new ConfigProfiles(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals/profiles"));
    public static final UtilityPreferences UTILITIES=new UtilityPreferences(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals-utilities.json"));
    public static final AuctionStore AUCTIONS = new AuctionStore(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals-auctions.json"));
    public static final CapeStore CAPES=new CapeStore(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals-capes.json"));
    public static final OutfitStore OUTFITS=new OutfitStore(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals-outfits.json"));
    public static final GpsStore GPS_POINTS = new GpsStore(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals-gps.json"));
    public static final SoundLibrary SOUNDS = new SoundLibrary(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals/sounds"));
    public static KeyMapping openMenuKey;
    public static KeyMapping zoomKey;
    public static KeyMapping freeLookKey;
    public static KeyMapping changeHandKey, elytraSwapKey, fakePlayerKey;
    public static KeyMapping emoteKey,pairedEmoteKey;
    public static KeyMapping replayKey;
    public static KeyMapping tapeMouseKey;
    public static final WaypointStore WAYPOINTS = new WaypointStore(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals-waypoints.json"));
    public static final FriendStore FRIENDS = new FriendStore(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals-friends.json"));

    @Override
    public void onInitializeClient() {
        ClientErrors.initialize();
        ProtocolMenus.install(ProtocolUi::open);
        var startupPreset=new StartupPreset(FabricLoader.getInstance().getConfigDir());
        RenderPipelinesAccessor.toolvisuals$register(UiPipelines.TEXT);
        RenderPipelinesAccessor.toolvisuals$register(UiPipelines.RASTER_TEXT);
        ItemGlowPipeline.register();
        ClientTickEvents.END_CLIENT_TICK.register(HandItemGlow::tick);
        ArmorRenderTypes.initialize();
        AccessoryPipeline.initialize();
        CONFIG.load();
        CustomSounds.initialize();
        HolyWorldClient.initialize();
        OcclusionService.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(OcclusionService::tick);
        ClientTickEvents.END_CLIENT_TICK.register(BundledVoiceChat::tick);
        ClientTickEvents.END_CLIENT_TICK.register(UiFont::tickDisplay);
        ClientTickEvents.END_CLIENT_TICK.register(ChatResizer::tick);
        Keybinds.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(Keybinds::tick);
        ClientTickEvents.END_CLIENT_TICK.register(DropProtect::tick);
        ClientTickEvents.END_CLIENT_TICK.register(CrystalOptimizer::tick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->CrystalOptimizer.clear());
        PlayerRoster.initialize();
        Diagnostics.initialize(FabricLoader.getInstance().getConfigDir().resolve("tooldiagnostic.txt"),RuntimeMetadata::header);
        ClientTickEvents.START_CLIENT_TICK.register(FrameMonitor::beginTick);
        ClientTickEvents.START_CLIENT_TICK.register(TapeMouse::tick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client->{Diagnostics.close();TextureUploads.clear();});
        ClientLifecycleEvents.CLIENT_STOPPING.register(client->{try{CONFIG.flush();}catch(IOException error){LoggerFactory.getLogger("ToolVisuals").warn("Visual changes remain unsaved; original configuration preserved",error);}});
        UTILITIES.load();
        try{CAPES.load();}catch(IOException error){LoggerFactory.getLogger("ToolVisuals").warn("Cannot load cape library; original file preserved",error);}
        try{OUTFITS.load();}catch(IOException error){LoggerFactory.getLogger("ToolVisuals").warn("Cannot load outfits; original file preserved",error);}
        UiI18n.use(CONFIG.language());
        MultiplayerVersionsButton.initialize();
        ModuleLog.initialize();
        DiscordPresence.initialize();
        MusicClient.initialize();
        Optimizations.update(CONFIG);
        ACCOUNTS.load();
        try { WAYPOINTS.load(); }
        catch(IOException error) { LoggerFactory.getLogger("ToolVisuals").warn("Cannot load waypoints; original file preserved",error); }
        try { FRIENDS.load(); }
        catch(IOException error) { LoggerFactory.getLogger("ToolVisuals").warn("Cannot load friends; original file preserved",error); }
        AuctionHelper.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(NameProtect::tick);
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            PrivacyMode.tick(client);
            ChatTabs.tick(client);
            DurabilityAlert.tick(client);
        });
        ItemTooltipCallback.EVENT.register(ItemInfo::tooltip);
        ShulkerTooltips.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(client -> Optimizations.update(CONFIG));
        ClientTickEvents.END_CLIENT_TICK.register(TerrainSettings::tick);
        WorldEffects.initialize();
        Gps.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(Gps::tick);
        ClientTickEvents.END_CLIENT_TICK.register(ExtraTab::tick);
        if (FabricLoader.getInstance().isModLoaded("entityculling"))
            ClientTickEvents.END_CLIENT_TICK.register(EntityCullingBridge::tick);
        ClientTickEvents.END_CLIENT_TICK.register(ExtraHud::tick);
        ClientTickEvents.END_CLIENT_TICK.register(HealthIndicator::tick);
        ClientTickEvents.END_CLIENT_TICK.register(DamageVignette::tick);
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(ToolVisuals.MOD_ID, "controls"));
        openMenuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.toolvisuals.open_menu", InputConstants.Type.KEYBOARD, InputConstants.KEY_RSHIFT, category));
        zoomKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.toolvisuals.zoom", InputConstants.Type.KEYBOARD, InputConstants.KEY_C, category));
        freeLookKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.toolvisuals.free_look", InputConstants.Type.KEYBOARD, InputConstants.KEY_LALT, category));
        changeHandKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.toolvisuals.change_hand", InputConstants.Type.KEYBOARD, InputConstants.KEY_H, category));
        elytraSwapKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.toolvisuals.elytra_swap", InputConstants.Type.KEYBOARD, InputConstants.KEY_V, category));
        fakePlayerKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.toolvisuals.fake_player", InputConstants.Type.KEYBOARD, InputConstants.KEY_J, category));
        tapeMouseKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.toolvisuals.tape_mouse", InputConstants.Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), category));
        emoteKey=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.toolvisuals.emotes",InputConstants.Type.KEYBOARD,InputConstants.KEY_B,category));
        pairedEmoteKey=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.toolvisuals.paired_emotes",InputConstants.Type.KEYBOARD,InputConstants.KEY_K,category));
        replayKey=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.toolvisuals.replay",InputConstants.Type.KEYBOARD,InputConstants.KEY_F8,category));
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            while(replayKey.consumeClick())if(CONFIG.available(VisualModule.REPLAY)&&client.gui.overlay()==null&&client.isWindowActive()
                    &&!(client.gui.screen() instanceof ReplayViewerScreen)) {
                KeyMapping.releaseAll();client.gui.setScreen(new ReplayLibraryScreen(client.gui.screen()));
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(PairedEmoteInput::tick);
        Emotes.initialize();
        SocialClient.initialize();
        MiniGamesClient.initialize();
        CapeClient.initialize();
        FriendDataClient.initialize();
        AccessoryClient.initialize();
        ClientListClient.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(SocialClient::tick);
        ClientTickEvents.END_CLIENT_TICK.register(Emotes::tick);
        ExtraAnimations.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(ExtraAnimations::tick);
        ClientSendMessageEvents.MODIFY_COMMAND.register(CommandFix::outgoing);
        ClientSendMessageEvents.ALLOW_COMMAND.register(PvPSave::allowCommand);
        ClientTickEvents.END_CLIENT_TICK.register(CapeClient::tick);
        ClientTickEvents.END_CLIENT_TICK.register(AccessoryClient::tick);
        ClientTickEvents.END_CLIENT_TICK.register(ClientActions::tick);
        ClientTickEvents.END_CLIENT_TICK.register(SessionStats::tick);
        ToolPresents.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(ToolPresents::tick);
        ClientTickEvents.END_CLIENT_TICK.register(ReplayCapture::tick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ReplayCapture.shutdown());
        ClientTickEvents.END_CLIENT_TICK.register(FreeLook::update);
        ClientTickEvents.END_CLIENT_TICK.register(FrameMonitor::endTick);
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            startupPreset.apply(client);
            ReplayCapture.initialize(client);
            FontWarmup.start();
            ConnectionIdentity.initialize(client);
            if (CONFIG.needsMenuBindingMigration()) {
                if (openMenuKey.matches(InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT))) {
                    openMenuKey.setKey(openMenuKey.getDefaultKey());
                    KeyMapping.resetMapping();
                    client.options.save();
                }
                CONFIG.finishMenuBindingMigration();
            }
            if (CONFIG.needsZoomBindingMigration()) {
                if (zoomKey.matches(InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.KEY_Z))) {
                    zoomKey.setKey(zoomKey.getDefaultKey()); KeyMapping.resetMapping(); client.options.save();
                }
                CONFIG.finishZoomBindingMigration();
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client->ClientErrors.close());
    }

    public static boolean canOpenMenu(Minecraft client) {
        return openMenuKey != null && client.level != null && client.player != null
                && client.gui.screen() == null && client.gui.overlay() == null && client.isWindowActive();
    }

    public static void openMenu(Minecraft client) {
        KeyMapping.releaseAll();
        client.gui.setScreen(new VisualsScreen());
    }
}
