package com.viaversion.viafabricplus;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.Holder;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.BannerPattern;

import com.viaversion.viafabricplus.api.ViaFabricPlusBase;
import com.viaversion.viafabricplus.api.entrypoint.ViaFabricPlusLoadEntrypoint;
import com.viaversion.viafabricplus.api.events.ChangeProtocolVersionCallback;
import com.viaversion.viafabricplus.api.events.LoadingCycleCallback;
import com.viaversion.viafabricplus.api.events.LoadingCycleCallback.LoadingCycle;
import com.viaversion.viafabricplus.api.settings.SettingGroup;
import com.viaversion.viafabricplus.base.Events;
import com.viaversion.viafabricplus.base.sync_tasks.SyncTasks;
import com.viaversion.viafabricplus.features.FeaturesLoading;
import com.viaversion.viafabricplus.features.item.filter_creative_tabs.VersionedRegistries;
import com.viaversion.viafabricplus.features.item.negative_item_count.NegativeItemUtil;
import com.viaversion.viafabricplus.features.limitation.max_chat_length.MaxChatLength;
import com.viaversion.viafabricplus.injection.access.base.IConnection;
import com.viaversion.viafabricplus.injection.access.base.IServerData;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viafabricplus.protocoltranslator.translator.ItemTranslator;
import com.viaversion.viafabricplus.save.SaveManager;
import com.viaversion.viafabricplus.screen.impl.SettingsScreen;
import com.viaversion.viafabricplus.settings.SettingsManager;
import com.viaversion.viafabricplus.util.ChatUtil;
import com.viaversion.viafabricplus.util.ClassLoaderPriorityUtil;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.channel.Channel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import ru.he3hauka.toolvisuals.protocols.client.ProtocolMenus;

public final class ViaFabricPlusImpl implements ViaFabricPlusBase {
   public static final ViaFabricPlusImpl INSTANCE = new ViaFabricPlusImpl();
   private final Logger logger = LogManager.getLogger("ViaFabricPlus");
   private final Path path = FabricLoader.getInstance().getConfigDir().resolve("viafabricplus");
   private String version;
   private String implVersion;
   private CompletableFuture<Void> loadingFuture;

   public void init() {
      ViaFabricPlus.init(INSTANCE);
      ModMetadata metadata = ((ModContainer)FabricLoader.getInstance().getModContainer("viafabricplus").get()).getMetadata();
      this.version = metadata.getVersion().getFriendlyString();
      this.implVersion = metadata.getCustomValue("vfp:implVersion").getAsString();

      for (EntrypointContainer<ViaFabricPlusLoadEntrypoint> container : FabricLoader.getInstance()
         .getEntrypointContainers("viafabricplus", ViaFabricPlusLoadEntrypoint.class)) {
         ((ViaFabricPlusLoadEntrypoint)container.getEntrypoint()).onPlatformLoad(INSTANCE);
      }

      try {
         Files.createDirectories(this.path);
      } catch (IOException var4) {
         this.logger.error("Failed to create ViaFabricPlus directory", var4);
      }

      ClassLoaderPriorityUtil.loadOverridingJars(this.path, this.logger);
      SettingsManager.INSTANCE.init();
      SaveManager.INSTANCE.init();
      SyncTasks.init();
      FeaturesLoading.init();
      this.loadingFuture = ProtocolTranslator.init(this.path);
      Events.LOADING_CYCLE.register((LoadingCycleCallback)cycle -> {
         if (cycle == LoadingCycle.POST_GAME_LOAD) {
            this.loadingFuture.join();
            FeaturesLoading.postInit();
            SaveManager.INSTANCE.postInit();
         }
      });
      ((LoadingCycleCallback)Events.LOADING_CYCLE.invoker()).onLoadCycle(LoadingCycle.FINAL_LOAD);
   }

   public String getVersion() {
      return this.version;
   }

   public String getImplVersion() {
      return this.implVersion;
   }

   public Path getPath() {
      return this.path;
   }

   @Nullable
   public ProtocolVersion getTargetVersion() {
      return ProtocolTranslator.getTargetVersion();
   }

   public void setTargetVersion(ProtocolVersion newVersion) {
      ProtocolTranslator.setTargetVersion(newVersion);
   }

   @Nullable
   public ProtocolVersion getTargetVersion(Channel channel) {
      return ProtocolTranslator.getTargetVersion(channel);
   }

   @Nullable
   public ProtocolVersion getTargetVersion(Connection connection) {
      return ((IConnection)connection).viaFabricPlus$getTargetVersion();
   }

   public void setTargetVersion(ProtocolVersion newVersion, boolean revertOnDisconnect) {
      ProtocolTranslator.setTargetVersion(newVersion, revertOnDisconnect);
   }

   @Nullable
   public UserConnection getPlayNetworkUserConnection() {
      return ProtocolTranslator.getPlayNetworkUserConnection();
   }

   @Nullable
   public UserConnection getUserConnection(Connection connection) {
      return ((IConnection)connection).viaFabricPlus$getUserConnection();
   }

   @Nullable
   public ProtocolVersion getServerVersion(ServerData serverInfo) {
      return ((IServerData)serverInfo).viaFabricPlus$forcedVersion();
   }

   public void registerOnChangeProtocolVersionCallback(ChangeProtocolVersionCallback callback) {
      Events.CHANGE_PROTOCOL_VERSION.register(callback);
   }

   public void registerLoadingCycleCallback(LoadingCycleCallback callback) {
      Events.LOADING_CYCLE.register(callback);
   }

   public int getMaxChatLength(ProtocolVersion version) {
      return MaxChatLength.getChatLength();
   }

   public List<SettingGroup> getSettingGroups() {
      return Collections.unmodifiableList(SettingsManager.INSTANCE.getGroups());
   }

   public void addSettingGroup(SettingGroup group) {
      SettingsManager.INSTANCE.addGroup(group);
   }

   @Nullable
   public SettingGroup getSettingGroup(String translationKey) {
      for (SettingGroup group : SettingsManager.INSTANCE.getGroups()) {
         if (ChatUtil.uncoverTranslationKey(group.getName()).equals(translationKey)) {
            return group;
         }
      }

      return null;
   }

   public void openProtocolSelectionScreen(Screen parent) {
      Object var10001 = null;
      ProtocolMenus.open(parent);
   }

   public void openSettingsScreen(Screen parent) {
      SettingsScreen.INSTANCE.open(parent);
   }

   @Nullable
   public Item translateItem(ItemStack stack, ProtocolVersion targetVersion) {
      return ItemTranslator.mcToVia(stack, targetVersion);
   }

   @Nullable
   public ItemStack translateItem(Item item, ProtocolVersion sourceVersion) {
      return ItemTranslator.viaToMc(item, sourceVersion);
   }

   public boolean itemExists(net.minecraft.world.item.Item item, ProtocolVersion version) {
      return VersionedRegistries.containsItem(item, version);
   }

   public boolean enchantmentExists(ResourceKey<Enchantment> enchantment, ProtocolVersion version) {
      return VersionedRegistries.containsEnchantment(enchantment, version);
   }

   public boolean effectExists(Holder<MobEffect> effect, ProtocolVersion version) {
      return VersionedRegistries.containsEffect(effect, version);
   }

   public boolean bannerPatternExists(ResourceKey<BannerPattern> pattern, ProtocolVersion version) {
      return VersionedRegistries.containsBannerPattern(pattern, version);
   }

   public boolean itemExistsInConnection(net.minecraft.world.item.Item item) {
      return VersionedRegistries.keepItem(item);
   }

   public boolean itemExistsInConnection(ItemStack stack) {
      return VersionedRegistries.keepItem(stack);
   }

   public int getStackCount(ItemStack stack) {
      return NegativeItemUtil.getCount(stack);
   }

   public Logger getLogger() {
      return this.logger;
   }
}
