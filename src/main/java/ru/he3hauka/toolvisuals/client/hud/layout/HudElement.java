package ru.he3hauka.toolvisuals.client.hud.layout;

import java.util.Locale;

import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.*;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public enum HudElement {
    INFO(INFO_HUD), INFO_BRAND(INFO_HUD), INFO_PING(INFO_HUD), INFO_FPS(INFO_HUD), INFO_BPS(INFO_HUD), INFO_CLOCK(INFO_HUD), INFO_COORDINATES(INFO_HUD),
    MODULES(INFO_HUD), ARMOR(ARMOR_HUD), ARMOR_2(ARMOR_HUD), ARMOR_3(ARMOR_HUD), ARMOR_4(ARMOR_HUD),
    TARGET(TARGET_HUD, TARGET_HUD_X, TARGET_HUD_Y),
    HIT_HEALTH(HEALTH_INDICATOR, HEALTH_INDICATOR_X, HEALTH_INDICATOR_Y),
    KEYS(KEYSTROKES, KEYS_X, KEYS_Y),
    POTIONS(POTION_HUD, POTION_X, POTION_Y),
    BUFFS(BUFF_TRACKER,BUFF_X,BUFF_Y),
    INVENTORY(INVENTORY_HUD, INVENTORY_X, INVENTORY_Y),
    COOLDOWNS(COOLDOWN_HUD, COOLDOWN_X, COOLDOWN_Y),
    FRIENDS(FRIENDS_LIST, FRIENDS_LIST_X, FRIENDS_LIST_Y),
    CLIENTS(CLIENT_LIST, CLIENT_LIST_X, CLIENT_LIST_Y),
    SESSION(SESSION_STATS,SESSION_X,SESSION_Y),
    MUSIC(MUSIC_HUD,MUSIC_X,MUSIC_Y),
    PRESENTS(TOOL_PRESENTS, PRESENTS_X, PRESENTS_Y),
    BOSSES(BOSS_BAR_CUSTOMIZER, BOSS_X, BOSS_Y);

    public final String id;
    public final VisualModule module;
    public final VisualSetting xSetting, ySetting;
    HudElement(VisualModule module) { this(module, null, null); }
    HudElement(VisualModule module, VisualSetting xSetting, VisualSetting ySetting) {
        id = name().toLowerCase(Locale.ROOT); this.module = module;
        this.xSetting = xSetting; this.ySetting = ySetting;
    }
}
