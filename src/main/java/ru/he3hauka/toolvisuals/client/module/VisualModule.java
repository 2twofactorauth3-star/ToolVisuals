package ru.he3hauka.toolvisuals.client.module;

import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.localization.UiI18n;

public enum VisualModule {
    NO_RENDER("NoRender", ModuleCategory.WORLD),
    WORLD_CUSTOMIZER("WorldCustomizer", ModuleCategory.WORLD),
    FULL_BRIGHT("FullBright", ModuleCategory.WORLD),
    WORLD_PARTICLES("WorldParticles", ModuleCategory.WORLD),
    WAYPOINTS("Waypoints", ModuleCategory.WORLD),
    TOOL_PRESENTS("ToolPresents", ModuleCategory.WORLD),
    REPLAY("Replay", ModuleCategory.WORLD),
    OPTIMIZATION("Optimization", ModuleCategory.RENDER),
    MOTION_BLUR("MotionBlur", ModuleCategory.RENDER),
    BLOCK_HIGHLIGHT("BlockOverlay", ModuleCategory.RENDER),
    DAMAGE_VIGNETTE("DamageVignette", ModuleCategory.RENDER),
    AUTO_SPRINT("AutoSprint", ModuleCategory.PLAYER),
    TAPE_MOUSE("TapeMouse", ModuleCategory.PLAYER),
    CHANGE_HAND("ChangeHand", ModuleCategory.PLAYER),
    COMMAND_FIX("CommandFix", ModuleCategory.PLAYER),
    ELYTRA_SWAP("ElytraSwap", ModuleCategory.PLAYER),
    FAKE_PLAYER("FakePlayer", ModuleCategory.PLAYER),
    FAST_EXP("FastExp", ModuleCategory.PLAYER),
    ITEM_SCROLLER("ItemScroller", ModuleCategory.PLAYER),
    MINI_GAMES("MiniGames", ModuleCategory.PLAYER),
    DROP_PROTECT("DropProtect", ModuleCategory.PLAYER),
    PVP_SAVE("PvPSave", ModuleCategory.PLAYER),
    AUCTION_HELPER("AuctionHelper", ModuleCategory.PLAYER),
    CUSTOM_HAND("CustomHand", ModuleCategory.PLAYER),
    JUMP_CIRCLES("JumpCircles", ModuleCategory.PLAYER),
    TRAILS("Trails", ModuleCategory.PLAYER),
    CHINA_HAT("ChinaHat", ModuleCategory.PLAYER),
    ZOOM("Zoom", ModuleCategory.PLAYER),
    NAME_PROTECT("NameProtect", ModuleCategory.PLAYER),
    NO_HURT_CAM("NoHurtCam", ModuleCategory.RENDER),
    PROJECTILE_PREDICTION("ProjectilePrediction", ModuleCategory.RENDER),
    ITEM_PHYSIC("ItemPhysic", ModuleCategory.RENDER),
    TOTEM_POP_COUNTER("TotemPopCounter", ModuleCategory.RENDER),
    SOUND_CUSTOMIZER("SoundCustomizer", ModuleCategory.PLAYER),
    NO_SWING_ANIMATION("NoSwing", ModuleCategory.RENDER),
    CHUNK_ANIMATOR("ChunkAnimator", ModuleCategory.RENDER),
    MINI_ITEMS("MiniItems", ModuleCategory.RENDER),
    FREE_LOOK("FreeLook", ModuleCategory.RENDER),
    NO_FLUID("NoFluid", ModuleCategory.RENDER),
    FAST_SHADERS("FastShaders", ModuleCategory.RENDER),
    ARMOR_COLOR("ArmorColor", ModuleCategory.RENDER),
    HIT_COLOR("HitColor", ModuleCategory.RENDER),
    CUSTOM_HITBOXES("CustomHitboxes", ModuleCategory.RENDER),
    HIT_PARTICLES("HitParticles", ModuleCategory.RENDER),
    ASPECT_RATIO("AspectRatio", ModuleCategory.RENDER),
    TARGET_ESP("TargetESP", ModuleCategory.RENDER),
    HIT_BUBBLES("HitBubbles", ModuleCategory.RENDER),
    CROSSHAIR("Crosshair", ModuleCategory.HUD),
    BETTER_HUD("BetterHUD", ModuleCategory.HUD),
    INFO_HUD("InfoHUD", ModuleCategory.HUD),
    APPLE_SKIN("AppleSkin", ModuleCategory.HUD),
    HEALTH_INDICATOR("HealthIndicator", ModuleCategory.HUD),
    ARMOR_HUD("ArmorHUD", ModuleCategory.HUD),
    EXTRA_TAB("ExtraTAB", ModuleCategory.HUD),
    INVENTORY_HUD("InventoryHUD", ModuleCategory.HUD),
    TARGET_HUD("TargetHUD", ModuleCategory.HUD),
    KEYSTROKES("Keystrokes", ModuleCategory.HUD),
    POTION_HUD("PotionHUD", ModuleCategory.HUD),
    COOLDOWN_HUD("CooldownHUD", ModuleCategory.HUD),
    FRIENDS_LIST("FriendsList", ModuleCategory.HUD),
    CLIENT_LIST("ClientList", ModuleCategory.HUD),
    SESSION_STATS("SessionStats", ModuleCategory.HUD),
    MUSIC_HUD("MusicHUD", ModuleCategory.HUD),
    CHAT_TABS("ChatTabs", ModuleCategory.HUD),
    DURABILITY_ALERT("DurabilityAlert", ModuleCategory.PLAYER),
    ITEM_INFO("ItemInfo", ModuleCategory.PLAYER),
    SHULKER_TOOLTIP("ShulkerTooltip", ModuleCategory.PLAYER),
    BOSS_BAR_CUSTOMIZER("BossBarCustomizer", ModuleCategory.HUD),
    STREAMER_MODE("StreamerMode", ModuleCategory.RENDER),
    RESOURCE_PACK_SPOOFER("ResourcePackSpoofer", ModuleCategory.PLAYER),
    ANIMATIONS("Animations", ModuleCategory.PLAYER),
    EXTRA_ANIMATIONS("ExtraAnimations", ModuleCategory.PLAYER),
    DEATH_ASCENSION("DeathAscension", ModuleCategory.RENDER),
    CAPES("Capes", ModuleCategory.PLAYER),
    COSMETICS("Cosmetics", ModuleCategory.PLAYER),
    CLIENT_BADGE("ClientBadge", ModuleCategory.PLAYER),
    KEYBINDS("Keybinds",ModuleCategory.PLAYER),
    VOICE_CHAT("VoiceChat",ModuleCategory.PLAYER),
    CRYSTAL_OPTIMIZER("CrystalOptimizer",ModuleCategory.PLAYER);

    public final String title;
    public final String id;
    public final ModuleCategory category;

    VisualModule(String title, ModuleCategory category) {
        this.title = title;
        this.id = name().toLowerCase(java.util.Locale.ROOT);
        this.category = category;
    }

    public Component description() { return UiI18n.tr("toolvisuals.module." + id); }
}
