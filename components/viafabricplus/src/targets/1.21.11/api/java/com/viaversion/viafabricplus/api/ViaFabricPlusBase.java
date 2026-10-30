package com.viaversion.viafabricplus.api;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.Holder;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.BannerPattern;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viafabricplus.api.events.ChangeProtocolVersionCallback;
import com.viaversion.viafabricplus.api.events.LoadingCycleCallback;
import com.viaversion.viafabricplus.api.settings.SettingGroup;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.channel.Channel;
import java.nio.file.Path;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public interface ViaFabricPlusBase {

    default int apiVersion() {
        return 6;
    }

    String getVersion();

    String getImplVersion();

    Path getPath();

    ProtocolVersion getTargetVersion();

    void setTargetVersion(final ProtocolVersion newVersion);

    ProtocolVersion getTargetVersion(final Channel channel);

    ProtocolVersion getTargetVersion(final Connection connection);

    void setTargetVersion(final ProtocolVersion newVersion, final boolean revertOnDisconnect);

    @Nullable UserConnection getPlayNetworkUserConnection();

    @Nullable UserConnection getUserConnection(final Connection connection);

    @Nullable ProtocolVersion getServerVersion(final ServerData serverInfo);

    void registerOnChangeProtocolVersionCallback(final ChangeProtocolVersionCallback callback);

    void registerLoadingCycleCallback(final LoadingCycleCallback callback);

    int getMaxChatLength(final ProtocolVersion version);

    List<SettingGroup> getSettingGroups();

    void addSettingGroup(final SettingGroup group);

    @Nullable SettingGroup getSettingGroup(final String translationKey);

    void openProtocolSelectionScreen(final Screen parent);

    void openSettingsScreen(final Screen parent);

    @Nullable Item translateItem(final ItemStack stack, final ProtocolVersion targetVersion);

    @Nullable ItemStack translateItem(final Item item, final ProtocolVersion sourceVersion);

    boolean itemExists(final net.minecraft.world.item.Item item, final ProtocolVersion version);

    boolean enchantmentExists(final ResourceKey<Enchantment> enchantment, final ProtocolVersion version);

    boolean effectExists(final Holder<MobEffect> effect, final ProtocolVersion version);

    boolean bannerPatternExists(final ResourceKey<BannerPattern> pattern, final ProtocolVersion version);

    boolean itemExistsInConnection(final net.minecraft.world.item.Item item);

    boolean itemExistsInConnection(final ItemStack stack);

    int getStackCount(final ItemStack stack);

}
