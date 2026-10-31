package ru.he3hauka.toolvisuals.client;

import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import ru.he3hauka.toolvisuals.ToolVisuals;
import ru.he3hauka.toolvisuals.client.armor.render.ArmorRenderTypes;
import ru.he3hauka.toolvisuals.client.camera.FreeLook;
import ru.he3hauka.toolvisuals.client.command.CommandFix;
import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.config.ConfigProfiles;
import ru.he3hauka.toolvisuals.client.connection.AccountStore;
import ru.he3hauka.toolvisuals.client.connection.ConnectionIdentity;
import ru.he3hauka.toolvisuals.client.connection.ui.ProtocolUi;
import ru.he3hauka.toolvisuals.protocols.client.ProtocolMenus;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsScreen;
import ru.he3hauka.toolvisuals.client.gui.render.UiPipelines;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.menu.multiplayer.MultiplayerVersionsButton;
import ru.he3hauka.toolvisuals.client.nameprotect.NameProtect;
import ru.he3hauka.toolvisuals.client.player.ClientActions;
import ru.he3hauka.toolvisuals.client.pvp.PvPSave;
import ru.he3hauka.toolvisuals.mixin.render.RenderPipelinesAccessor;

public final class ToolVisualsClient implements ClientModInitializer {
    public static final ClientConfig CONFIG = new ClientConfig(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals.json"));
    public static final AccountStore ACCOUNTS = new AccountStore(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals-accounts.json"));
    public static final ConfigProfiles PROFILES = new ConfigProfiles(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals/profiles"));
    public static final ru.he3hauka.toolvisuals.client.utility.UtilityPreferences UTILITIES=new ru.he3hauka.toolvisuals.client.utility.UtilityPreferences(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals-utilities.json"));
    public static final ru.he3hauka.toolvisuals.client.auction.AuctionStore AUCTIONS = new ru.he3hauka.toolvisuals.client.auction.AuctionStore(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals-auctions.json"));
    public static final ru.he3hauka.toolvisuals.client.cape.CapeStore CAPES=new ru.he3hauka.toolvisuals.client.cape.CapeStore(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals-capes.json"));
    public static final ru.he3hauka.toolvisuals.client.cosmetic.outfit.OutfitStore OUTFITS=new ru.he3hauka.toolvisuals.client.cosmetic.outfit.OutfitStore(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals-outfits.json"));
    public static KeyMapping openMenuKey;
    public static KeyMapping zoomKey;
    public static KeyMapping freeLookKey;
    public static KeyMapping changeHandKey, elytraSwapKey, fakePlayerKey;
    public static KeyMapping emoteKey,pairedEmoteKey;
    public static KeyMapping replayKey;
    public static KeyMapping tapeMouseKey;
    public static final ru.he3hauka.toolvisuals.client.waypoint.WaypointStore WAYPOINTS = new ru.he3hauka.toolvisuals.client.waypoint.WaypointStore(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals-waypoints.json"));
    public static final ru.he3hauka.toolvisuals.client.friends.FriendStore FRIENDS = new ru.he3hauka.toolvisuals.client.friends.FriendStore(
            FabricLoader.getInstance().getConfigDir().resolve("toolvisuals-friends.json"));

    @Override
    public void onInitializeClient() {
        ClientErrors.initialize();
        ProtocolMenus.install(ProtocolUi::open);
        var startupPreset=new ru.he3hauka.toolvisuals.client.optimization.preset.StartupPreset(FabricLoader.getInstance().getConfigDir());
        RenderPipelinesAccessor.toolvisuals$register(UiPipelines.TEXT);
        RenderPipelinesAccessor.toolvisuals$register(UiPipelines.RASTER_TEXT);
        ArmorRenderTypes.initialize();
        ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.AccessoryPipeline.initialize();
        CONFIG.load();
        ru.he3hauka.toolvisuals.client.connection.holyworld.HolyWorldClient.initialize();
        ru.he3hauka.toolvisuals.client.optimization.occlusion.OcclusionService.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.optimization.occlusion.OcclusionService::tick);
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.voice.BundledVoiceChat::tick);
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.gui.render.UiFont::tickDisplay);
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.chat.ChatResizer::tick);
        ru.he3hauka.toolvisuals.client.keybinds.Keybinds.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.keybinds.Keybinds::tick);
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.items.dropprotect.DropProtect::tick);
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.optimization.crystal.CrystalOptimizer::tick);
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->ru.he3hauka.toolvisuals.client.optimization.crystal.CrystalOptimizer.clear());
        ru.he3hauka.toolvisuals.client.optimization.hud.PlayerRoster.initialize();
        ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.initialize(FabricLoader.getInstance().getConfigDir().resolve("tooldiagnostic.txt"),ru.he3hauka.toolvisuals.client.optimization.diagnostics.report.RuntimeMetadata::header);
        ClientTickEvents.START_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.optimization.frame.FrameMonitor::beginTick);
        ClientTickEvents.START_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.input.tape.TapeMouse::tick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client->{ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.close();ru.he3hauka.toolvisuals.client.optimization.texture.TextureUploads.clear();});
        ClientLifecycleEvents.CLIENT_STOPPING.register(client->{try{CONFIG.flush();}catch(java.io.IOException error){org.slf4j.LoggerFactory.getLogger("ToolVisuals").warn("Visual changes remain unsaved; original configuration preserved",error);}});
        UTILITIES.load();
        try{CAPES.load();}catch(java.io.IOException error){org.slf4j.LoggerFactory.getLogger("ToolVisuals").warn("Cannot load cape library; original file preserved",error);}
        try{OUTFITS.load();}catch(java.io.IOException error){org.slf4j.LoggerFactory.getLogger("ToolVisuals").warn("Cannot load outfits; original file preserved",error);}
        UiI18n.use(CONFIG.language());
        MultiplayerVersionsButton.initialize();
        ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.initialize();
        ru.he3hauka.toolvisuals.client.discord.DiscordPresence.initialize();
        ru.he3hauka.toolvisuals.client.music.MusicClient.initialize();
        ru.he3hauka.toolvisuals.client.optimization.Optimizations.update(CONFIG);
        ACCOUNTS.load();
        try { WAYPOINTS.load(); }
        catch(java.io.IOException error) { org.slf4j.LoggerFactory.getLogger("ToolVisuals").warn("Cannot load waypoints; original file preserved",error); }
        try { FRIENDS.load(); }
        catch(java.io.IOException error) { org.slf4j.LoggerFactory.getLogger("ToolVisuals").warn("Cannot load friends; original file preserved",error); }
        ru.he3hauka.toolvisuals.client.auction.AuctionHelper.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(NameProtect::tick);
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            ru.he3hauka.toolvisuals.client.utility.privacy.PrivacyMode.tick(client);
            ru.he3hauka.toolvisuals.client.utility.chat.ChatTabs.tick(client);
            ru.he3hauka.toolvisuals.client.utility.durability.DurabilityAlert.tick(client);
        });
        net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register(ru.he3hauka.toolvisuals.client.utility.items.ItemInfo::tooltip);
        ru.he3hauka.toolvisuals.client.items.shulkertooltip.ShulkerTooltips.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(client -> ru.he3hauka.toolvisuals.client.optimization.Optimizations.update(CONFIG));
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.optimization.terrain.TerrainSettings::tick);
        ru.he3hauka.toolvisuals.client.effects.WorldEffects.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.hud.tab.ExtraTab::tick);
        if (FabricLoader.getInstance().isModLoaded("entityculling"))
            ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.optimization.occlusion.EntityCullingBridge::tick);
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.hud.ExtraHud::tick);
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.hud.health.HealthIndicator::tick);
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.render.vignette.DamageVignette::tick);
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
            while(replayKey.consumeClick())if(CONFIG.available(ru.he3hauka.toolvisuals.client.module.VisualModule.REPLAY)&&client.gui.overlay()==null&&client.isWindowActive()
                    &&!(client.gui.screen() instanceof ru.he3hauka.toolvisuals.client.replay.ui.ReplayViewerScreen)) {
                KeyMapping.releaseAll();client.gui.setScreen(new ru.he3hauka.toolvisuals.client.replay.ui.ReplayLibraryScreen(client.gui.screen()));
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.social.PairedEmoteInput::tick);
        ru.he3hauka.toolvisuals.client.animation.Emotes.initialize();
        ru.he3hauka.toolvisuals.client.social.SocialClient.initialize();
        ru.he3hauka.toolvisuals.client.minigame.MiniGamesClient.initialize();
        ru.he3hauka.toolvisuals.client.cape.CapeClient.initialize();
        ru.he3hauka.toolvisuals.client.cosmetic.accessory.AccessoryClient.initialize();
        ru.he3hauka.toolvisuals.client.social.presence.ClientListClient.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.social.SocialClient::tick);
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.animation.Emotes::tick);
        ru.he3hauka.toolvisuals.client.animation.movement.ExtraAnimations.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.animation.movement.ExtraAnimations::tick);
        net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents.MODIFY_COMMAND.register(CommandFix::outgoing);
        net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents.ALLOW_COMMAND.register(PvPSave::allowCommand);
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.cape.CapeClient::tick);
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.cosmetic.accessory.AccessoryClient::tick);
        ClientTickEvents.END_CLIENT_TICK.register(ClientActions::tick);
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.session.SessionStats::tick);
        ru.he3hauka.toolvisuals.client.presents.ToolPresents.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.presents.ToolPresents::tick);
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.replay.capture.ReplayCapture::tick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ru.he3hauka.toolvisuals.client.replay.capture.ReplayCapture.shutdown());
        ClientTickEvents.END_CLIENT_TICK.register(FreeLook::update);
        ClientTickEvents.END_CLIENT_TICK.register(ru.he3hauka.toolvisuals.client.optimization.frame.FrameMonitor::endTick);
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            startupPreset.apply(client);
            ru.he3hauka.toolvisuals.client.replay.capture.ReplayCapture.initialize(client);
            ru.he3hauka.toolvisuals.client.gui.text.FontWarmup.start();
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
