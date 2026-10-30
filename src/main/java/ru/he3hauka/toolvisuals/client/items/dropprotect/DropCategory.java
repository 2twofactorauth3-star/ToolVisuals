package ru.he3hauka.toolvisuals.client.items.dropprotect;

import java.util.Set;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public enum DropCategory {
    ENCHANTED_BOOKS(DROP_ENCHANTED_BOOKS, true),
    ENCHANTED_ITEMS(DROP_ENCHANTED_ITEMS, false),
    POTIONS(DROP_POTIONS, true),
    EXPERIENCE_BOTTLES(DROP_EXPERIENCE_BOTTLES, true),
    TOTEMS(DROP_TOTEMS, true),
    GOLDEN_APPLES(DROP_GOLDEN_APPLES, true),
    ELYTRA(DROP_ELYTRA, true),
    DIAMONDS(DROP_DIAMONDS, true),
    NETHERITE(DROP_NETHERITE, true),
    EMERALDS(DROP_EMERALDS, true),
    CONTAINERS(DROP_CONTAINERS, true),
    VALUABLE_MATERIALS(DROP_VALUABLE_MATERIALS, true),
    COMBAT_ITEMS(DROP_COMBAT_ITEMS, true),
    NAMED_ITEMS(DROP_NAMED_ITEMS, false),
    OTHER_RARE(DROP_OTHER_RARE, false),
    EQUIPMENT(DROP_EQUIPMENT, false);

    private static final Set<String> MATERIALS = Set.of("nether_star", "dragon_egg", "dragon_head", "heart_of_the_sea",
            "echo_shard", "shulker_shell", "heavy_core", "wither_skeleton_skull", "breeze_rod", "trial_key",
            "ominous_trial_key", "recovery_compass", "conduit", "beacon");
    private static final Set<String> COMBAT = Set.of("ender_pearl", "ender_eye", "end_crystal", "respawn_anchor", "trident", "mace");

    public final VisualSetting setting;
    private final boolean dedicated;

    DropCategory(VisualSetting setting, boolean dedicated) {
        this.setting = setting;
        this.dedicated = dedicated;
    }

    public static boolean protects(ClientConfig config, ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (config.value(DROP_MODE) == 0) return true;
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = id.getNamespace().equals("minecraft") ? id.getPath() : "";
        boolean dedicated = false, otherRare = false;
        for (var category : values()) {
            if (category == OTHER_RARE) { otherRare = config.flag(category.setting); continue; }
            if (!category.matches(stack, path)) continue;
            if (config.flag(category.setting)) return true;
            dedicated |= category.dedicated;
        }
        return !dedicated && otherRare && stack.getOrDefault(DataComponents.RARITY, Rarity.COMMON) != Rarity.COMMON;
    }

    public static boolean visible(ClientConfig config, VisualSetting setting) {
        if (config.value(DROP_MODE) != 0) return true;
        for (var category : values()) if (category.setting == setting) return false;
        return true;
    }

    private boolean matches(ItemStack stack, String path) {
        return switch (this) {
            case ENCHANTED_BOOKS -> path.equals("enchanted_book") || stack.has(DataComponents.STORED_ENCHANTMENTS);
            case ENCHANTED_ITEMS -> stack.isEnchanted();
            case POTIONS -> stack.has(DataComponents.POTION_CONTENTS);
            case EXPERIENCE_BOTTLES -> path.equals("experience_bottle") || path.equals("ominous_bottle");
            case TOTEMS -> path.equals("totem_of_undying");
            case GOLDEN_APPLES -> path.equals("golden_apple") || path.equals("enchanted_golden_apple");
            case ELYTRA -> path.equals("elytra");
            case DIAMONDS -> path.equals("diamond") || path.startsWith("diamond_");
            case NETHERITE -> path.startsWith("netherite_");
            case EMERALDS -> path.equals("emerald") || path.equals("emerald_block");
            case CONTAINERS -> path.endsWith("shulker_box") || path.equals("ender_chest") || path.equals("bundle") || path.endsWith("_bundle");
            case VALUABLE_MATERIALS -> MATERIALS.contains(path) || path.endsWith("_smithing_template") || path.endsWith("_banner_pattern");
            case COMBAT_ITEMS -> COMBAT.contains(path);
            case NAMED_ITEMS -> stack.has(DataComponents.CUSTOM_NAME);
            case EQUIPMENT -> stack.isDamageableItem();
            case OTHER_RARE -> false;
        };
    }
}
