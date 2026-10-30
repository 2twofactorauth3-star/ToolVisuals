package ru.he3hauka.toolvisuals.client.render.filter;

import java.util.Locale;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.entity.BlockEntity;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

public final class NoRender {
    private NoRender() {
    }

    public static boolean hides(RenderOption option) {
        return ToolVisualsClient.CONFIG.hides(option);
    }

    public static boolean hidesEntity(Entity entity) {
        if (!ToolVisualsClient.CONFIG.enabled()) {
            return false;
        }
        if (hides(RenderOption.ALL_ENTITIES)
                || entity instanceof Player && hides(RenderOption.PLAYERS)
                || entity instanceof Projectile && hides(RenderOption.PROJECTILES)) {
            return true;
        }
        if (entity instanceof Mob) {
            return hides(entity.getType().getCategory() == MobCategory.MONSTER
                    ? RenderOption.HOSTILE_MOBS : RenderOption.PASSIVE_MOBS);
        }
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
        return switch (id) {
            case "item" -> hides(RenderOption.DROPPED_ITEMS);
            case "experience_orb" -> hides(RenderOption.EXPERIENCE_ORBS);
            case "armor_stand" -> hides(RenderOption.ARMOR_STANDS);
            case "item_frame", "glow_item_frame" -> hides(RenderOption.ITEM_FRAMES);
            case "painting" -> hides(RenderOption.PAINTINGS);
            case "lightning_bolt" -> hides(RenderOption.LIGHTNING);
            case "tnt" -> hides(RenderOption.TNT);
            case "falling_block" -> hides(RenderOption.FALLING_BLOCKS);
            default -> (id.contains("boat") || id.contains("minecart")) && hides(RenderOption.VEHICLES);
        };
    }

    public static boolean hidesBlockEntity(BlockEntity entity) {
        if (!ToolVisualsClient.CONFIG.enabled()) {
            return false;
        }
        if (hides(RenderOption.ALL_BLOCK_ENTITIES)) {
            return true;
        }
        String id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(entity.getType()).getPath();
        return switch (id) {
            case "chest", "trapped_chest", "ender_chest", "shulker_box" -> hides(RenderOption.CHESTS);
            case "sign", "hanging_sign" -> hides(RenderOption.SIGNS);
            case "banner" -> hides(RenderOption.BANNERS);
            case "beacon" -> hides(RenderOption.BEACONS);
            case "mob_spawner", "trial_spawner" -> hides(RenderOption.SPAWNERS);
            default -> false;
        };
    }

    public static boolean hidesParticle(String name) {
        if (!ToolVisualsClient.CONFIG.enabled()) {
            return false;
        }
        if (hides(RenderOption.ALL_PARTICLES)) {
            return true;
        }
        if (!hides(RenderOption.SMOKE_PARTICLES) && !hides(RenderOption.EXPLOSION_PARTICLES)
                && !hides(RenderOption.FIREWORK_PARTICLES) && !hides(RenderOption.POTION_PARTICLES)
                && !hides(RenderOption.PORTAL_PARTICLES) && !hides(RenderOption.TOTEM_PARTICLES)
                && !hides(RenderOption.BLOCK_PARTICLES)) {
            return false;
        }
        String id = name.toLowerCase(Locale.ROOT);
        return (id.contains("smoke") || id.contains("ash")) && hides(RenderOption.SMOKE_PARTICLES)
                || (id.contains("explosion") || id.contains("explode") || id.contains("gust")) && hides(RenderOption.EXPLOSION_PARTICLES)
                || (id.contains("firework") || id.contains("flash")) && hides(RenderOption.FIREWORK_PARTICLES)
                || (id.contains("effect") || id.contains("spell") || id.contains("dragonbreath") || id.contains("dragon_breath")) && hides(RenderOption.POTION_PARTICLES)
                || id.contains("portal") && hides(RenderOption.PORTAL_PARTICLES)
                || id.contains("totem") && hides(RenderOption.TOTEM_PARTICLES)
                || (id.contains("terrain") || id.contains("block") || id.contains("fallingdust") || id.contains("falling_dust")) && hides(RenderOption.BLOCK_PARTICLES);
    }
}
