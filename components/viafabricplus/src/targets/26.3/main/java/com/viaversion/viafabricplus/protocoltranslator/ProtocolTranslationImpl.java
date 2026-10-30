package com.viaversion.viafabricplus.protocoltranslator;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.viaversion.viaaprilfools.ViaAprilFoolsPlatformImpl;
import com.viaversion.viabackwards.ViaBackwardsPlatformImpl;
import com.viaversion.viafabricplus.api.protocoltranslator.ProtocolTranslation;
import com.viaversion.viafabricplus.injection.access.core.IConnection;
import com.viaversion.viafabricplus.injection.access.core.IServerData;
import com.viaversion.viafabricplus.protocoltranslator.impl.command.ViaFabricPlusCommandHandler;
import com.viaversion.viafabricplus.protocoltranslator.impl.platform.ViaFabricPlusViaLegacyPlatform;
import com.viaversion.viafabricplus.protocoltranslator.impl.platform.ViaFabricPlusViaVersionPlatform;
import com.viaversion.viafabricplus.protocoltranslator.impl.viaversion.ViaFabricPlusPlatformLoader;
import com.viaversion.viafabricplus.protocoltranslator.protocol.ViaFabricPlusProtocol;
import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.platform.NoopInjector;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.Connection;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import ru.he3hauka.toolvisuals.client.connection.ProtocolPolicy;

public final class ProtocolTranslationImpl implements ProtocolTranslation {
   public static final AttributeKey<Connection> MINECRAFT_CONNECTION_ATTRIBUTE_KEY = AttributeKey.newInstance("viafabricplus-minecraft-connection");
   public static final AttributeKey<ProtocolVersion> TARGET_VERSION_ATTRIBUTE_KEY = AttributeKey.newInstance("viafabricplus-target-version");
   public static final ProtocolVersion NATIVE_VERSION = ProtocolVersion.v26_3;
   private final List<BiConsumer<ProtocolVersion, ProtocolVersion>> changeVersionListeners = new ArrayList<>();
   private ProtocolVersion targetVersion = ProtocolPolicy.normalizeNullable(NATIVE_VERSION, NATIVE_VERSION);
   private ProtocolVersion previousVersion = null;

   public ProtocolVersion targetVersion() {
      return this.targetVersion;
   }

   public ProtocolVersion targetVersion(Connection connection) {
      return ((IConnection)connection).viaFabricPlus$getTargetVersion();
   }

   public ProtocolVersion targetVersion(Channel channel) {
      return (ProtocolVersion)channel.attr(TARGET_VERSION_ATTRIBUTE_KEY).get();
   }

   public void setTargetVersion(ProtocolVersion targetVersion, boolean revertOnDisconnect) {
      targetVersion = ProtocolPolicy.normalize(targetVersion, NATIVE_VERSION);
      ProtocolVersion oldVersion = this.targetVersion;
      this.targetVersion = ProtocolPolicy.normalizeNullable(targetVersion, NATIVE_VERSION);
      if (oldVersion != targetVersion) {
         if (revertOnDisconnect) {
            this.previousVersion = oldVersion;
         }

         for (BiConsumer<ProtocolVersion, ProtocolVersion> listener : this.changeVersionListeners) {
            listener.accept(oldVersion, this.targetVersion);
         }
      }
   }

   public void addChangeProtocolVersionListener(BiConsumer<ProtocolVersion, ProtocolVersion> listener) {
      this.changeVersionListeners.add(listener);
   }

   @Nullable
   public UserConnection userConnection() {
      ClientPacketListener handler = Minecraft.getInstance().getConnection();
      return handler != null ? ((IConnection)handler.getConnection()).viaFabricPlus$getUserConnection() : null;
   }

   @Nullable
   public UserConnection userConnection(Connection connection) {
      return ((IConnection)connection).viaFabricPlus$getUserConnection();
   }

   @Nullable
   public ProtocolVersion serverVersion(ServerData serverData) {
      return ((IServerData)serverData).viaFabricPlus$forcedVersion();
   }

   public void setServerVersion(ServerData serverData, ProtocolVersion version) {
      ((IServerData)serverData).viaFabricPlus$forceVersion(version);
   }

   public void injectionPreviousVersionHandler(Channel channel) {
      if (this.previousVersion != null) {
         channel.closeFuture().addListener(var1x -> {
            this.setTargetVersion(this.previousVersion);
            this.previousVersion = null;
         });
      }
   }

   public CompletableFuture<Void> init(Path path) {
      if (SharedConstants.getProtocolVersion() != NATIVE_VERSION.getOriginalVersion()) {
         throw new IllegalStateException("Native version is not the same as the current version");
      } else {
         ClientCommandRegistrationCallback.EVENT
            .register(
               (ClientCommandRegistrationCallback)(dispatcher, registryAccess) -> {
                  ViaFabricPlusCommandHandler commandHandler = (ViaFabricPlusCommandHandler)Via.getManager().getCommandHandler();
                  RequiredArgumentBuilder<FabricClientCommandSource, String> executor = ((RequiredArgumentBuilder)RequiredArgumentBuilder.<FabricClientCommandSource, String>argument(
                           "args", StringArgumentType.greedyString()
                        )
                        .executes(commandHandler::execute))
                     .suggests(commandHandler::suggestion);
                  dispatcher.register(
                     (LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.<FabricClientCommandSource>literal("viafabricplus").then(executor))
                        .executes(commandHandler::execute)
                  );
               }
            );
         return CompletableFuture.runAsync(
            () -> {
               ViaManagerImpl.initAndLoad(
                  new ViaFabricPlusViaVersionPlatform(path.toFile()),
                  new NoopInjector(),
                  new ViaFabricPlusCommandHandler(),
                  new ViaFabricPlusPlatformLoader(),
                  new Runnable[]{() -> {
                     new ViaBackwardsPlatformImpl();
                     new ViaFabricPlusViaLegacyPlatform();
                     new ViaAprilFoolsPlatformImpl();
                  }}
               );
               ProtocolVersion.register(ProtocolTranslation.AUTO_DETECT_VERSION);
               ViaFabricPlusProtocol.INSTANCE.initialize();
            },
            Util.backgroundExecutor()
         );
      }
   }
}
