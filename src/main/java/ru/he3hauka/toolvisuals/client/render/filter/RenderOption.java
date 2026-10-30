package ru.he3hauka.toolvisuals.client.render.filter;

import java.util.Locale;

import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.localization.UiI18n;

public enum RenderOption {
    RAIN(RenderCategory.WORLD, false),
    SNOW(RenderCategory.WORLD, false),
    CLOUDS(RenderCategory.WORLD, false),
    SKY(RenderCategory.WORLD, false),
    SUN(RenderCategory.WORLD, false),
    MOON(RenderCategory.WORLD, false),
    STARS(RenderCategory.WORLD, false),
    SUNRISE(RenderCategory.WORLD, false),
    END_FLASH(RenderCategory.WORLD, false),
    LIGHTNING_FLASH(RenderCategory.WORLD, false),
    WORLD_BORDER(RenderCategory.WORLD, false),
    BLOCK_OUTLINE(RenderCategory.WORLD, false),
    BLOCK_BREAKING(RenderCategory.WORLD, false),
    FOG(RenderCategory.WORLD, false),
    ALL_ENTITIES(RenderCategory.ENTITIES, false),
    PLAYERS(RenderCategory.ENTITIES, false),
    HOSTILE_MOBS(RenderCategory.ENTITIES, false),
    PASSIVE_MOBS(RenderCategory.ENTITIES, false),
    DROPPED_ITEMS(RenderCategory.ENTITIES, false),
    EXPERIENCE_ORBS(RenderCategory.ENTITIES, false),
    ARMOR_STANDS(RenderCategory.ENTITIES, false),
    ITEM_FRAMES(RenderCategory.ENTITIES, false),
    PAINTINGS(RenderCategory.ENTITIES, false),
    PROJECTILES(RenderCategory.ENTITIES, false),
    LIGHTNING(RenderCategory.ENTITIES, false),
    TNT(RenderCategory.ENTITIES, false),
    FALLING_BLOCKS(RenderCategory.ENTITIES, false),
    VEHICLES(RenderCategory.ENTITIES, false),
    ALL_BLOCK_ENTITIES(RenderCategory.ENTITIES, false),
    CHESTS(RenderCategory.ENTITIES, false),
    SIGNS(RenderCategory.ENTITIES, false),
    BANNERS(RenderCategory.ENTITIES, false),
    BEACONS(RenderCategory.ENTITIES, false),
    SPAWNERS(RenderCategory.ENTITIES, false),
    ALL_PARTICLES(RenderCategory.EFFECTS, false),
    SMOKE_PARTICLES(RenderCategory.EFFECTS, false),
    EXPLOSION_PARTICLES(RenderCategory.EFFECTS, false),
    FIREWORK_PARTICLES(RenderCategory.EFFECTS, false),
    POTION_PARTICLES(RenderCategory.EFFECTS, false),
    PORTAL_PARTICLES(RenderCategory.EFFECTS, false),
    TOTEM_PARTICLES(RenderCategory.EFFECTS, false),
    BLOCK_PARTICLES(RenderCategory.EFFECTS, false),
    ENTITY_SHADOWS(RenderCategory.EFFECTS, false),
    NAME_TAGS(RenderCategory.EFFECTS, false),
    ENTITY_FIRE(RenderCategory.EFFECTS, false),
    LEASHES(RenderCategory.EFFECTS, false),
    GLOWING(RenderCategory.EFFECTS, false),
    FIRE_OVERLAY(RenderCategory.EFFECTS, true),
    WATER_OVERLAY(RenderCategory.EFFECTS, false),
    TOTEM_ANIMATION(RenderCategory.EFFECTS, false),
    EQUIPMENT_OVERLAY(RenderCategory.EFFECTS, false),
    FREEZING_OVERLAY(RenderCategory.EFFECTS, false),
    SPYGLASS_OVERLAY(RenderCategory.EFFECTS, false),
    VIGNETTE(RenderCategory.EFFECTS, true),
    HURT_CAMERA(RenderCategory.CAMERA, true),
    VIEW_BOBBING(RenderCategory.CAMERA, false),
    NAUSEA(RenderCategory.CAMERA, true),
    PORTAL_DISTORTION(RenderCategory.CAMERA, true),
    FIRST_PERSON_HANDS(RenderCategory.CAMERA, false),
    POST_EFFECTS(RenderCategory.CAMERA, false),
    ALL_HUD(RenderCategory.HUD, false),
    BOSS_BARS(RenderCategory.HUD, false),
    SCOREBOARD(RenderCategory.HUD, false),
    CHAT(RenderCategory.HUD, false),
    PLAYER_LIST(RenderCategory.HUD, false),
    CROSSHAIR(RenderCategory.HUD, false),
    HOTBAR(RenderCategory.HUD, false),
    HEALTH(RenderCategory.HUD, false),
    ARMOR(RenderCategory.HUD, false),
    FOOD(RenderCategory.HUD, false),
    AIR(RenderCategory.HUD, false),
    VEHICLE_HEALTH(RenderCategory.HUD, false),
    STATUS_EFFECTS(RenderCategory.HUD, false),
    TITLES(RenderCategory.HUD, false),
    ACTION_BAR(RenderCategory.HUD, false),
    SUBTITLES(RenderCategory.HUD, false),
    ITEM_NAMES(RenderCategory.HUD, false),
    DEBUG_OVERLAY(RenderCategory.HUD, false),
    SLEEP_OVERLAY(RenderCategory.HUD, false),
    EXPERIENCE_BAR(RenderCategory.HUD, false),
    LOCATOR_BAR(RenderCategory.HUD, false),
    DEMO_OVERLAY(RenderCategory.HUD, false),
    SAVING_INDICATOR(RenderCategory.HUD, false);

    public final RenderCategory category;
    public final boolean hiddenByDefault;
    public final String id = name().toLowerCase(Locale.ROOT);

    RenderOption(RenderCategory category, boolean hiddenByDefault) {
        this.category = category;
        this.hiddenByDefault = hiddenByDefault;
    }

    public Component label() {
        return UiI18n.tr("toolvisuals.option." + id);
    }

    public Component description() {
        return UiI18n.tr("toolvisuals.option." + id + ".description");
    }
}

