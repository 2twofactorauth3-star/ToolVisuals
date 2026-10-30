package com.viaversion.viafabricplus.protocoltranslator;

import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.viaversion.viafabricplus.api.events.ChangeProtocolVersionCallback;
import com.viaversion.viafabricplus.base.Events;
import com.viaversion.viafabricplus.injection.access.base.IClientConnection;
import com.viaversion.viafabricplus.protocoltranslator.impl.command.ViaFabricPlusVLCommandHandler;
import com.viaversion.viafabricplus.protocoltranslator.impl.platform.ViaFabricPlusViaLegacyPlatformImpl;
import com.viaversion.viafabricplus.protocoltranslator.impl.platform.ViaFabricPlusViaVersionPlatformImpl;
import com.viaversion.viafabricplus.protocoltranslator.impl.viaversion.ViaFabricPlusVLInjector;
import com.viaversion.viafabricplus.protocoltranslator.impl.viaversion.ViaFabricPlusVLLoader;
import com.viaversion.viafabricplus.protocoltranslator.netty.ViaFabricPlusVLLegacyPipeline;
import com.viaversion.viafabricplus.protocoltranslator.protocol.ViaFabricPlusProtocol;
import com.viaversion.viafabricplus.protocoltranslator.util.NoPacketSendChannel;
import com.viaversion.vialoader.ViaLoader;
import com.viaversion.vialoader.impl.platform.ViaAprilFoolsPlatformImpl;
import com.viaversion.vialoader.impl.platform.ViaBackwardsPlatformImpl;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.ProtocolPathEntry;
import com.viaversion.viaversion.api.protocol.ProtocolPipeline;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionType;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.util.AttributeKey;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import ru.he3hauka.toolvisuals.client.connection.ProtocolPolicy;

public final class ProtocolTranslator {
   public static final AttributeKey<ClientConnection> CLIENT_CONNECTION_ATTRIBUTE_KEY = AttributeKey.newInstance("viafabricplus-clientconnection");
   public static final AttributeKey<ProtocolVersion> TARGET_VERSION_ATTRIBUTE_KEY = AttributeKey.newInstance("viafabricplus-targetversion");
   public static final ProtocolVersion NATIVE_VERSION = ProtocolVersion.v1_21_4;
   public static final ProtocolVersion AUTO_DETECT_PROTOCOL = new ProtocolVersion(VersionType.SPECIAL, -2, -1, "Auto Detect (1.7+ servers)", null) {
      protected Comparator<ProtocolVersion> customComparator() {
         return (o1, o2) -> {
            if (o1 == ProtocolTranslator.AUTO_DETECT_PROTOCOL) {
               return 1;
            } else {
               return o2 == ProtocolTranslator.AUTO_DETECT_PROTOCOL ? -1 : 0;
            }
         };
      }

      public boolean isKnown() {
         return false;
      }
   };
   private static ProtocolVersion targetVersion = NATIVE_VERSION;
   private static ProtocolVersion previousVersion = null;

   public static void injectViaPipeline(ClientConnection var0, Channel var1) {
      IClientConnection var2 = (IClientConnection)var0;
      ProtocolVersion var3 = var2.viaFabricPlus$getTargetVersion();
      if (var3 != NATIVE_VERSION) {
         var1.attr(CLIENT_CONNECTION_ATTRIBUTE_KEY).set(var0);
         var1.attr(TARGET_VERSION_ATTRIBUTE_KEY).set(var3);
         Object var10001 = null;
         boolean var10000 = false;
         UserConnectionImpl var4 = new UserConnectionImpl(var1, true);
         new ProtocolPipelineImpl(var4);
         var2.viaFabricPlus$setUserConnection(var4);
         var1.pipeline().addLast(new ChannelHandler[]{new ViaFabricPlusVLLegacyPipeline(var4, var3)});
      }
   }

   public static ProtocolVersion getTargetVersion() {
      return targetVersion;
   }

   public static ProtocolVersion getTargetVersion(Channel channel) {
      if (channel != null && channel.hasAttr(TARGET_VERSION_ATTRIBUTE_KEY)) {
         return (ProtocolVersion)channel.attr(TARGET_VERSION_ATTRIBUTE_KEY).get();
      } else {
         throw new IllegalStateException("Target version attribute not set");
      }
   }

   public static void setTargetVersion(ProtocolVersion newVersion) {
      setTargetVersion(newVersion, false);
   }

   public static void setTargetVersion(ProtocolVersion newVersion, boolean revertOnDisconnect) {
      newVersion = ProtocolPolicy.normalize(newVersion, NATIVE_VERSION);
      if (newVersion != null) {
         ProtocolVersion oldVersion = targetVersion;
         targetVersion = newVersion;
         if (oldVersion != newVersion) {
            if (revertOnDisconnect) {
               previousVersion = oldVersion;
            }

            ((ChangeProtocolVersionCallback)Events.CHANGE_PROTOCOL_VERSION.invoker()).onChangeProtocolVersion(oldVersion, targetVersion);
         }
      }
   }

   public static void injectPreviousVersionReset(Channel channel) {
      if (previousVersion != null) {
         channel.closeFuture().addListener(future -> {
            setTargetVersion(previousVersion);
            previousVersion = null;
         });
      }
   }

   public static UserConnection createDummyUserConnection(ProtocolVersion clientVersion, ProtocolVersion serverVersion) {
      UserConnection user = new UserConnectionImpl(NoPacketSendChannel.INSTANCE, true);
      ProtocolPipeline pipeline = new ProtocolPipelineImpl(user);
      List<ProtocolPathEntry> path = Via.getManager().getProtocolManager().getProtocolPath(clientVersion, serverVersion);
      if (path != null) {
         for (ProtocolPathEntry pair : path) {
            pipeline.add(pair.protocol());
            pair.protocol().init(user);
         }
      }

      ProtocolInfo info = user.getProtocolInfo();
      info.setState(State.PLAY);
      info.setProtocolVersion(clientVersion);
      info.setServerProtocolVersion(serverVersion);
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player != null) {
         GameProfile profile = mc.player.getGameProfile();
         info.setUsername(profile.getName());
         info.setUuid(profile.getId());
      }

      return user;
   }

   public static UserConnection getPlayNetworkUserConnection() {
      ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
      return handler == null ? null : ((IClientConnection)handler.getConnection()).viaFabricPlus$getUserConnection();
   }

   private static void patchConfigs(Path path) {
      try {
         Path viaVersionConfig = path.resolve("viaversion.yml");
         Files.writeString(
            viaVersionConfig,
            "fix-infested-block-breaking: false\nshield-blocking: false\nno-delay-shield-blocking: true\nhandle-invalid-item-count: true\n",
            StandardOpenOption.CREATE_NEW
         );
      } catch (FileAlreadyExistsException var4) {
      } catch (Throwable var5) {
         throw new RuntimeException("Failed to patch ViaVersion config", var5);
      }

      try {
         Path viaLegacyConfig = path.resolve("vialegacy.yml");
         Files.writeString(viaLegacyConfig, "legacy-skull-loading: true\nlegacy-skin-loading: true\n", StandardOpenOption.CREATE_NEW);
      } catch (FileAlreadyExistsException var2) {
      } catch (Throwable var3) {
         throw new RuntimeException("Failed to patch ViaLegacy config", var3);
      }
   }

   public static CompletableFuture<Void> init(Path path) {
      if (SharedConstants.getProtocolVersion() != NATIVE_VERSION.getOriginalVersion()) {
         throw new IllegalStateException("Native version is not the same as the current version");
      } else {
         patchConfigs(path);
         ClientCommandRegistrationCallback.EVENT
            .register(
               (ClientCommandRegistrationCallback)(dispatcher, registryAccess) -> {
                  ViaFabricPlusVLCommandHandler commandHandler = (ViaFabricPlusVLCommandHandler)Via.getManager().getCommandHandler();
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
               ViaLoader.init(
                  new ViaFabricPlusViaVersionPlatformImpl(path.toFile()),
                  new ViaFabricPlusVLLoader(),
                  new ViaFabricPlusVLInjector(),
                  new ViaFabricPlusVLCommandHandler(),
                  new Supplier[]{ViaBackwardsPlatformImpl::new, ViaFabricPlusViaLegacyPlatformImpl::new, ViaAprilFoolsPlatformImpl::new}
               );
               ProtocolVersion.register(AUTO_DETECT_PROTOCOL);
               ViaFabricPlusProtocol.INSTANCE.initialize();
            }
         );
      }
   }
}
