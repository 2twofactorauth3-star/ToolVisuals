package com.viaversion.viafabricplus.visuals.settings;

import net.minecraft.text.Text;

import com.viaversion.viafabricplus.api.settings.SettingGroup;
import com.viaversion.viafabricplus.api.settings.type.BooleanSetting;
import com.viaversion.viafabricplus.api.settings.type.ModeSetting;
import com.viaversion.viafabricplus.api.settings.type.VersionedBooleanSetting;
import com.viaversion.vialoader.util.VersionRange;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;

public final class VisualSettings extends SettingGroup {

    public static final VisualSettings INSTANCE = new VisualSettings();

    public final ModeSetting changeGameMenuScreenLayout = new ModeSetting(this, Text.translatable("visual_settings.viafabricplus.change_game_menu_screen_layout"),
            Text.translatable("change_game_menu_screen_layout.viafabricplus.authentic"),
            Text.translatable("change_game_menu_screen_layout.viafabricplus.adjusted"),
            Text.translatable("base.viafabricplus.off")
    );
    public final BooleanSetting removeBubblePopSound = new BooleanSetting(this, Text.translatable("visual_settings.viafabricplus.remove_bubble_pop_sound"), false);
    public final BooleanSetting hideEmptyBubbleIcons = new BooleanSetting(this, Text.translatable("visual_settings.viafabricplus.hide_empty_bubble_icons"), false);
    public final BooleanSetting hideVillagerProfession = new BooleanSetting(this, Text.translatable("visual_settings.viafabricplus.hide_villager_profession"), false);

    public final VersionedBooleanSetting hideDownloadTerrainScreenTransitionEffects = new VersionedBooleanSetting(this, Text.translatable("visual_settings.viafabricplus.hide_download_terrain_screen_transition_effects"), VersionRange.andOlder(ProtocolVersion.v1_20_5));

    public final VersionedBooleanSetting lockBlockingArmRotation = new VersionedBooleanSetting(this, Text.translatable("visual_settings.viafabricplus.lock_blocking_arm_rotation"), VersionRange.andOlder(ProtocolVersion.v1_20_2));

    public final VersionedBooleanSetting changeBodyRotationInterpolation = new VersionedBooleanSetting(this, Text.translatable("visual_settings.viafabricplus.change_body_rotation_interpolation"), VersionRange.andOlder(ProtocolVersion.v1_19_3));

    public final VersionedBooleanSetting disableSecureChatWarning = new VersionedBooleanSetting(this, Text.translatable("visual_settings.viafabricplus.disable_secure_chat_warning"), VersionRange.andOlder(ProtocolVersion.v1_19));

    public final VersionedBooleanSetting hideSignatureIndicator = new VersionedBooleanSetting(this, Text.translatable("visual_settings.viafabricplus.hide_signature_indicator"), VersionRange.andOlder(ProtocolVersion.v1_18_2));

    public final VersionedBooleanSetting replacePetrifiedOakSlab = new VersionedBooleanSetting(this, Text.translatable("visual_settings.viafabricplus.replace_petrified_oak_slab"), VersionRange.of(LegacyProtocolVersion.r1_3_1tor1_3_2, ProtocolVersion.v1_12_2));
    public final VersionedBooleanSetting hideFurnaceRecipeBook = new VersionedBooleanSetting(this, Text.translatable("visual_settings.viafabricplus.hide_furnace_recipe_book"), VersionRange.andOlder(ProtocolVersion.v1_12_2));
    public final VersionedBooleanSetting forceUnicodeFontForNonAsciiLanguages = new VersionedBooleanSetting(this, Text.translatable("visual_settings.viafabricplus.force_unicode_font_for_non_ascii_languages"), VersionRange.andOlder(ProtocolVersion.v1_12_2));
    public final VersionedBooleanSetting sneakInstantly = new VersionedBooleanSetting(this, Text.translatable("visual_settings.viafabricplus.sneak_instantly"), VersionRange.andOlder(ProtocolVersion.v1_12_2));

    public final VersionedBooleanSetting sidewaysBackwardsRunning = new VersionedBooleanSetting(this, Text.translatable("visual_settings.viafabricplus.sideways_backwards_walking"), VersionRange.andOlder(ProtocolVersion.v1_11_1));
    public final VersionedBooleanSetting hideCraftingRecipeBook = new VersionedBooleanSetting(this, Text.translatable("visual_settings.viafabricplus.hide_crafting_recipe_book"), VersionRange.andOlder(ProtocolVersion.v1_11_1));

    public final VersionedBooleanSetting alwaysRenderCrosshair = new VersionedBooleanSetting(this, Text.translatable("visual_settings.viafabricplus.always_render_crosshair"), VersionRange.andOlder(ProtocolVersion.v1_8));

    public final VersionedBooleanSetting swingHandOnItemUse = new VersionedBooleanSetting(this, Text.translatable("visual_settings.viafabricplus.swing_hand_on_item_use"), VersionRange.andOlder(ProtocolVersion.v1_7_6));
    public final VersionedBooleanSetting tiltItemPositions = new VersionedBooleanSetting(this, Text.translatable("visual_settings.viafabricplus.tilt_item_positions"), VersionRange.andOlder(ProtocolVersion.v1_7_6));
    public final VersionedBooleanSetting enableLegacyTablist = new VersionedBooleanSetting(this, Text.translatable("visual_settings.viafabricplus.enable_legacy_tablist"), VersionRange.andOlder(ProtocolVersion.v1_7_6));

    public final VersionedBooleanSetting replaceHurtSoundWithOOFSound = new VersionedBooleanSetting(this, Text.translatable("visual_settings.viafabricplus.replace_hurt_sound_with_oof_sound"), VersionRange.andOlder(LegacyProtocolVersion.b1_8tob1_8_1));

    public final VersionedBooleanSetting hideModernHUDElements = new VersionedBooleanSetting(this, Text.translatable("visual_settings.viafabricplus.hide_modern_hud_elements"), VersionRange.andOlder(LegacyProtocolVersion.b1_7tob1_7_3));

    public final VersionedBooleanSetting replaceCreativeInventory = new VersionedBooleanSetting(this, Text.translatable("visual_settings.viafabricplus.replace_creative_inventory_with_classic_inventory"), VersionRange.andOlder(LegacyProtocolVersion.c0_28toc0_30));
    public final VersionedBooleanSetting oldWalkingAnimation = new VersionedBooleanSetting(this, Text.translatable("visual_settings.viafabricplus.old_walking_animation"), VersionRange.andOlder(LegacyProtocolVersion.c0_28toc0_30));

    public VisualSettings() {
        super(Text.translatable("setting_group_name.viafabricplus.visual"));
        changeGameMenuScreenLayout.setTooltip(() -> switch (changeGameMenuScreenLayout.getIndex()) {
            case 0 -> Text.translatable("change_game_menu_screen_layout.viafabricplus.authentic.tooltip");
            case 1 -> Text.translatable("change_game_menu_screen_layout.viafabricplus.adjusted.tooltip");
            default -> Text.translatable("change_game_menu_screen_layout.viafabricplus.off.tooltip");
        });
        changeGameMenuScreenLayout.setValue(1);

        hideDownloadTerrainScreenTransitionEffects.setValue(VersionedBooleanSetting.DISABLED_INDEX);
        forceUnicodeFontForNonAsciiLanguages.setValue(VersionedBooleanSetting.DISABLED_INDEX);
    }

}
