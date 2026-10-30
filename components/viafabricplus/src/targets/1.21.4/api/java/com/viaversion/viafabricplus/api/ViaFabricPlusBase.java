package com.viaversion.viafabricplus.api;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;

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
        return 2;
    }

    String getVersion();

    String getImplVersion();

    Path rootPath();

    ProtocolVersion getTargetVersion();

    ProtocolVersion getTargetVersion(final Channel channel);

    ProtocolVersion getTargetVersion(final ClientConnection connection);

    void setTargetVersion(final ProtocolVersion newVersion);

    void setTargetVersion(final ProtocolVersion newVersion, final boolean revertOnDisconnect);

    @Nullable UserConnection getPlayNetworkUserConnection();

    @Nullable UserConnection getUserConnection(final ClientConnection connection);

    @Nullable ProtocolVersion getServerVersion(final ServerInfo serverInfo);

    void registerOnChangeProtocolVersionCallback(final ChangeProtocolVersionCallback callback);

    void registerLoadingCycleCallback(final LoadingCycleCallback callback);

    int getMaxChatLength(final ProtocolVersion version);

    List<SettingGroup> settingGroups();

    void addSettingGroup(final SettingGroup group);

    @Nullable SettingGroup getSettingGroup(final String translationKey);

    void openProtocolSelectionScreen(final Screen parent);

    void openSettingsScreen(final Screen parent);

    @Nullable Item translateItem(final ItemStack stack, final ProtocolVersion targetVersion);

    @Nullable ItemStack translateItem(final Item item, final ProtocolVersion sourceVersion);

    boolean itemExists(final net.minecraft.item.Item item, final ProtocolVersion version);

    boolean itemExistsInConnection(final net.minecraft.item.Item item);

    int getStackCount(final ItemStack stack);

}
