package com.viaversion.viafabricplus.settings.impl;

import net.minecraft.network.chat.Component;

import com.viaversion.viafabricplus.api.settings.SettingGroup;
import com.viaversion.viafabricplus.api.settings.type.BooleanSetting;
import com.viaversion.viafabricplus.api.settings.type.VersionedBooleanSetting;
import com.viaversion.viafabricplus.features.font.FontCacheReload;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersionRange;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;

public final class DebugSettings extends SettingGroup {

    public static final DebugSettings INSTANCE = new DebugSettings();

    public final BooleanSetting queueConfigPackets = new BooleanSetting(this, Component.translatable("debug_settings.viafabricplus.queue_config_packets"), true);
    public final BooleanSetting printNetworkingErrorsToLogs = new BooleanSetting(this, Component.translatable("debug_settings.viafabricplus.print_networking_errors_to_logs"), true);
    public final BooleanSetting ignoreFabricSyncErrors = new BooleanSetting(this, Component.translatable("debug_settings.viafabricplus.ignore_fabric_sync_errors"), false);
    public final BooleanSetting hideModernJigsawScreenFeatures = new BooleanSetting(this, Component.translatable("debug_settings.viafabricplus.hide_modern_jigsaw_screen_features"), true);
    public final BooleanSetting filterNonExistingGlyphs = new BooleanSetting(this, Component.translatable("debug_settings.viafabricplus.filter_non_existing_glyphs"), true) {
        @Override
        public void onValueChanged() {
            FontCacheReload.reload();
        }
    };

    public final VersionedBooleanSetting dontCreatePacketErrorCrashReports = new VersionedBooleanSetting(this, Component.translatable("debug_settings.viafabricplus.dont_create_packet_error_crash_reports"), ProtocolVersionRange.andOlder(ProtocolVersion.v1_20_3));

    public final VersionedBooleanSetting disableSequencing = new VersionedBooleanSetting(this, Component.translatable("debug_settings.viafabricplus.disable_sequencing"), ProtocolVersionRange.andOlder(ProtocolVersion.v1_18_2));

    public final VersionedBooleanSetting alwaysTickClientPlayer = new VersionedBooleanSetting(this, Component.translatable("debug_settings.viafabricplus.always_tick_client_player"), ProtocolVersionRange.andOlder(ProtocolVersion.v1_8).add(ProtocolVersionRange.andNewer(ProtocolVersion.v1_17)));

    public final VersionedBooleanSetting executeInputsSynchronously = new VersionedBooleanSetting(this, Component.translatable("debug_settings.viafabricplus.execute_inputs_synchronously"), ProtocolVersionRange.andOlder(ProtocolVersion.v1_12_2));
    public final VersionedBooleanSetting legacyTabCompletions = new VersionedBooleanSetting(this, Component.translatable("debug_settings.viafabricplus.legacy_tab_completions"), ProtocolVersionRange.andOlder(ProtocolVersion.v1_12_2));
    public final VersionedBooleanSetting legacyPaneOutlines = new VersionedBooleanSetting(this, Component.translatable("debug_settings.viafabricplus.legacy_pane_outlines"), ProtocolVersionRange.andOlder(ProtocolVersion.v1_12_2));

    public final VersionedBooleanSetting emulateArmorHud = new VersionedBooleanSetting(this, Component.translatable("debug_settings.viafabricplus.emulate_armor_hud"), ProtocolVersionRange.andOlder(ProtocolVersion.v1_8));
    public final VersionedBooleanSetting hideModernCommandBlockScreenFeatures = new VersionedBooleanSetting(this, Component.translatable("debug_settings.viafabricplus.hide_modern_command_block_screen_features"), ProtocolVersionRange.andOlder(ProtocolVersion.v1_8));
    public final VersionedBooleanSetting legacyCropOutlines = new VersionedBooleanSetting(this, Component.translatable("debug_settings.viafabricplus.legacy_crop_outlines"), ProtocolVersionRange.andOlder(ProtocolVersion.v1_8));
    public final VersionedBooleanSetting serversidePlaceSounds = new VersionedBooleanSetting(this, Component.translatable("debug_settings.viafabricplus.serverside_place_sounds"), ProtocolVersionRange.andOlder(ProtocolVersion.v1_8));

    public final VersionedBooleanSetting disableServerPinging = new VersionedBooleanSetting(this, Component.translatable("debug_settings.viafabricplus.disable_server_pinging"), ProtocolVersionRange.andOlder(LegacyProtocolVersion.b1_7tob1_7_3));

    public DebugSettings() {
        super(Component.translatable("setting_group_name.viafabricplus.debug"));
    }

}
