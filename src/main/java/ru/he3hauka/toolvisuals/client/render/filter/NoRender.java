package ru.he3hauka.toolvisuals.client.render.filter;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.entity.BlockEntity;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.render.particles.ParticleVisibility;

public final class NoRender {
    private NoRender() {
    }

    public static boolean hides(RenderOption option) {
        return ToolVisualsClient.CONFIG.hides(option)
                || ParticleVisibility.hidesOptimized(ToolVisualsClient.CONFIG, option)
                || ru.he3hauka.toolvisuals.client.render.weather.PrecipitationVisibility.hides(ToolVisualsClient.CONFIG, option);
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
        return ParticleVisibility.hides(ToolVisualsClient.CONFIG, name);
    }
}
