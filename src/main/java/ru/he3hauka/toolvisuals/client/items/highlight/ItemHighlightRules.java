package ru.he3hauka.toolvisuals.client.items.highlight;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Pattern;

public final class ItemHighlightRules {
    public static final int LIMIT = 512;
    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+");
    public enum Tier {
        LEGENDARY(0xffbe46), VERY_RARE(0xff555f), EPIC(0xe35bff), UNCOMMON(0x559dff);
        public final int color;
        Tier(int color) { this.color = color; }
    }
    public final Map<String, Integer> colors;

    public ItemHighlightRules(Map<String, Integer> colors) {
        Objects.requireNonNull(colors);
        if (colors.size() > LIMIT) throw new IllegalArgumentException("Too many highlighted items");
        colors.forEach((id, color) -> {
            if (!validId(id) || color == null || color < 0 || color > 0xffffff)
                throw new IllegalArgumentException("Invalid item highlight rule");
        });
        this.colors = Collections.unmodifiableMap(new TreeMap<>(colors));
    }

    public static boolean validId(String id) { return id != null && id.length() <= 128 && ID.matcher(id).matches() && !id.equals("minecraft:air"); }
    public ItemHighlightRules with(String id, int color) {
        var next = new LinkedHashMap<>(colors); next.put(id, color); return new ItemHighlightRules(next);
    }
    public ItemHighlightRules without(String id) {
        var next = new LinkedHashMap<>(colors); next.remove(id); return new ItemHighlightRules(next);
    }
    public static ItemHighlightRules defaults() {
        var colors = new LinkedHashMap<String, Integer>();
        add(colors, Tier.LEGENDARY, "totem_of_undying", "enchanted_golden_apple", "nether_star", "dragon_egg", "elytra", "netherite_block", "netherite_ingot", "netherite_upgrade_smithing_template");
        add(colors, Tier.VERY_RARE, "ancient_debris", "netherite_scrap", "netherite_sword", "netherite_pickaxe", "netherite_axe", "netherite_shovel", "netherite_hoe", "netherite_helmet", "netherite_chestplate", "netherite_leggings", "netherite_boots", "mace", "heavy_core", "end_crystal");
        add(colors, Tier.EPIC, "diamond_sword", "diamond_pickaxe", "diamond_axe", "diamond_shovel", "diamond_hoe", "diamond_helmet", "diamond_chestplate", "diamond_leggings", "diamond_boots", "diamond_block", "emerald_block", "golden_apple", "enchanted_book", "trident", "beacon", "heart_of_the_sea", "conduit", "ender_chest", "echo_shard", "dragon_breath");
        add(colors, Tier.UNCOMMON, "diamond", "emerald", "gold_ingot", "gold_block", "iron_ingot", "lapis_lazuli", "amethyst_shard", "ender_pearl", "ender_eye", "blaze_rod", "breeze_rod", "ghast_tear", "experience_bottle", "shulker_shell", "nautilus_shell");
        return new ItemHighlightRules(colors);
    }
    private static void add(Map<String, Integer> colors, Tier tier, String... ids) {
        for (String id : ids) colors.put("minecraft:" + id, tier.color);
    }
}
