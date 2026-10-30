package com.viaversion.viafabricplus.protocoltranslator;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.util.Util;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.viaversion.viaaprilfools.ViaAprilFoolsPlatformImpl;
import com.viaversion.viabackwards.ViaBackwardsPlatformImpl;
import com.viaversion.viafabricplus.api.events.ChangeProtocolVersionCallback;
import com.viaversion.viafabricplus.base.Events;
import com.viaversion.viafabricplus.injection.access.base.IConnection;
import com.viaversion.viafabricplus.protocoltranslator.impl.command.ViaFabricPlusCommandHandler;
import com.viaversion.viafabricplus.protocoltranslator.impl.platform.ViaFabricPlusViaLegacyPlatform;
import com.viaversion.viafabricplus.protocoltranslator.impl.platform.ViaFabricPlusViaVersionPlatform;
import com.viaversion.viafabricplus.protocoltranslator.impl.viaversion.ViaFabricPlusPlatformLoader;
import com.viaversion.viafabricplus.protocoltranslator.netty.NoReadFlowControlHandler;
import com.viaversion.viafabricplus.protocoltranslator.netty.ViaFabricPlusDecoder;
import com.viaversion.viafabricplus.protocoltranslator.protocol.ViaFabricPlusProtocol;
import com.viaversion.viafabricplus.protocoltranslator.util.NoPacketSendChannel;
import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.ProtocolPathEntry;
import com.viaversion.viaversion.api.protocol.ProtocolPipeline;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionType;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.platform.NoopInjector;
import com.viaversion.viaversion.platform.ViaChannelInitializer;
import com.viaversion.viaversion.platform.ViaEncodeHandler;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.util.AttributeKey;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import net.raphimc.vialegacy.netty.PreNettyLengthPrepender;
import net.raphimc.vialegacy.netty.PreNettyLengthRemover;
import ru.he3hauka.toolvisuals.client.connection.ProtocolPolicy;

public final class ProtocolTranslator {
   public static final AttributeKey<Connection> CLIENT_CONNECTION_ATTRIBUTE_KEY = AttributeKey.newInstance("viafabricplus-clientconnection");
   public static final AttributeKey<ProtocolVersion> TARGET_VERSION_ATTRIBUTE_KEY = AttributeKey.newInstance("viafabricplus-targetversion");
   public static final ProtocolVersion NATIVE_VERSION = ProtocolVersion.v1_21_11;
   public static final String VIA_FLOW_CONTROL = "via-flow-control";
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

   public static void injectViaPipeline(Connection var0, Channel var1) {
      IConnection var2 = (IConnection)var0;
      ProtocolVersion var3 = var2.viaFabricPlus$getTargetVersion();
      var1.attr(CLIENT_CONNECTION_ATTRIBUTE_KEY).set(var0);
      var1.attr(TARGET_VERSION_ATTRIBUTE_KEY).set(var3);
      Object var10001 = null;
      boolean var10000 = false;
      UserConnection var4 = ViaChannelInitializer.createUserConnection(var1, true);
      var2.viaFabricPlus$setUserConnection(var4);
      ChannelPipeline var5 = var1.pipeline();
      var5.addBefore("inbound_config", "via-decoder", new ViaFabricPlusDecoder(var4));
      var5.addBefore("encoder", "via-encoder", new ViaEncodeHandler(var4));
      if (var3.olderThanOrEqualTo(LegacyProtocolVersion.r1_6_4)) {
         var5.addBefore("splitter", "vialegacy-pre-netty-length-prepender", new PreNettyLengthPrepender(var4));
         var5.addBefore("prepender", "vialegacy-pre-netty-length-remover", new PreNettyLengthRemover(var4));
      } else {
         var10001 = null;
         var10000 = false;
      }

      var5.addAfter("via-decoder", "via-flow-control", new NoReadFlowControlHandler());
      var4.getProtocolInfo().getPipeline().add(ViaFabricPlusProtocol.INSTANCE);
   }

   public static ProtocolVersion getTargetVersion() {
      return targetVersion;
   }

   public static ProtocolVersion getTargetVersion(Channel channel) {
      if (!channel.hasAttr(TARGET_VERSION_ATTRIBUTE_KEY)) {
         throw new IllegalStateException("ViaFabricPlus has not injected into that channel yet!");
      } else {
         return (ProtocolVersion)channel.attr(TARGET_VERSION_ATTRIBUTE_KEY).get();
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
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         GameProfile profile = mc.player.getGameProfile();
         info.setUsername(profile.name());
         info.setUuid(profile.id());
      }

      return user;
   }

   public static UserConnection getPlayNetworkUserConnection() {
      ClientPacketListener handler = Minecraft.getInstance().getConnection();
      return handler == null ? null : ((IConnection)handler.getConnection()).viaFabricPlus$getUserConnection();
   }

   public static CompletableFuture<Void> init(Path path) {
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
               ProtocolVersion.register(AUTO_DETECT_PROTOCOL);
               ViaFabricPlusProtocol.INSTANCE.initialize();
            },
            Util.backgroundExecutor()
         );
      }
   }
}
