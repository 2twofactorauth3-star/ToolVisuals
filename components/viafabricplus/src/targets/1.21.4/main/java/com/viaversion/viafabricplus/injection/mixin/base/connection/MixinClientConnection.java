package com.viaversion.viafabricplus.injection.mixin.base.connection;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.encryption.PacketDecryptor;
import net.minecraft.network.encryption.PacketEncryptor;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.profiler.MultiValueDebugSampleLogImpl;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.viaversion.viafabricplus.injection.access.base.IClientConnection;
import com.viaversion.viafabricplus.injection.access.base.IMultiValueDebugSampleLogImpl;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.vialoader.netty.CompressionReorderEvent;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import javax.crypto.Cipher;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientConnection.class)
public abstract class MixinClientConnection extends SimpleChannelInboundHandler<Packet<?>> implements IClientConnection {
   @Shadow
   public Channel channel;
   @Shadow
   private boolean encrypted;
   @Unique
   private UserConnection viaFabricPlus$userConnection;
   @Unique
   private ProtocolVersion viaFabricPlus$serverVersion;
   @Unique
   private Cipher viaFabricPlus$decryptionCipher;

   @Shadow
   public abstract void channelActive(@NotNull ChannelHandlerContext var1) throws Exception;

   @Inject(method = "setCompressionThreshold", at = @At("RETURN"))
   private void reorderCompression(int compressionThreshold, boolean rejectBad, CallbackInfo ci) {
      this.channel.pipeline().fireUserEventTriggered(CompressionReorderEvent.INSTANCE);
   }

   @Inject(method = "setupEncryption", at = @At("HEAD"), cancellable = true)
   private void storeDecryptionCipher(Cipher decryptionCipher, Cipher encryptionCipher, CallbackInfo ci) {
      if (this.viaFabricPlus$serverVersion != null && this.viaFabricPlus$serverVersion.olderThanOrEqualTo(LegacyProtocolVersion.r1_6_4)) {
         ci.cancel();
         this.viaFabricPlus$decryptionCipher = decryptionCipher;
         if (encryptionCipher == null) {
            throw new IllegalStateException("Encryption cipher is null");
         }

         this.encrypted = true;
         this.channel.pipeline().addBefore("vialegacy-pre-netty-length-remover", "encrypt", new PacketEncryptor(encryptionCipher));
      }
   }

   public void channelRegistered(ChannelHandlerContext var1) throws Exception {
      super.channelRegistered(var1);
      Object var10000 = null;
      boolean var2 = false;
   }

   @WrapWithCondition(
      method = "channelActive",
      at = @At(
         value = "INVOKE",
         target = "Lio/netty/channel/SimpleChannelInboundHandler;channelActive(Lio/netty/channel/ChannelHandlerContext;)V",
         remap = false
      )
   )
   private boolean dontCallChannelActiveTwice(SimpleChannelInboundHandler<Packet<?>> var1, ChannelHandlerContext var2) {
      Object var10000 = null;
      boolean var3 = false;
      return true;
   }

   @Inject(
      method = "connect(Ljava/net/InetSocketAddress;ZLnet/minecraft/util/profiler/MultiValueDebugSampleLogImpl;)Lnet/minecraft/network/ClientConnection;",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/network/ClientConnection;connect(Ljava/net/InetSocketAddress;ZLnet/minecraft/network/ClientConnection;)Lio/netty/channel/ChannelFuture;"
      )
   )
   private static void setTargetVersion(
      InetSocketAddress address, boolean useEpoll, MultiValueDebugSampleLogImpl packetSizeLog, CallbackInfoReturnable<ClientConnection> cir, @Local ClientConnection clientConnection
   ) {
      if (packetSizeLog instanceof IMultiValueDebugSampleLogImpl mixinMultiValueDebugSampleLogImpl
         && mixinMultiValueDebugSampleLogImpl.viaFabricPlus$getForcedVersion() != null) {
         ((IClientConnection)clientConnection).viaFabricPlus$setTargetVersion(mixinMultiValueDebugSampleLogImpl.viaFabricPlus$getForcedVersion());
      }
   }

   @WrapWithCondition(
      method = "connect(Ljava/net/InetSocketAddress;ZLnet/minecraft/util/profiler/MultiValueDebugSampleLogImpl;)Lnet/minecraft/network/ClientConnection;",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/network/ClientConnection;resetPacketSizeLog(Lnet/minecraft/util/profiler/MultiValueDebugSampleLogImpl;)V"
      )
   )
   private static boolean dontSetPerformanceLog(ClientConnection instance, MultiValueDebugSampleLogImpl packetSizeLog) {
      return !(
         packetSizeLog instanceof IMultiValueDebugSampleLogImpl mixinMultiValueDebugSampleLogImpl
            && mixinMultiValueDebugSampleLogImpl.viaFabricPlus$getForcedVersion() != null
      );
   }

   @Inject(method = "connect(Ljava/net/InetSocketAddress;ZLnet/minecraft/network/ClientConnection;)Lio/netty/channel/ChannelFuture;", at = @At("HEAD"))
   private static void setTargetVersion(InetSocketAddress address, boolean useEpoll, ClientConnection connection, CallbackInfoReturnable<ChannelFuture> cir) {
      ProtocolVersion targetVersion = ((IClientConnection)connection).viaFabricPlus$getTargetVersion();
      if (targetVersion == null) {
         targetVersion = ProtocolTranslator.getTargetVersion();
      }

      if (targetVersion == ProtocolTranslator.AUTO_DETECT_PROTOCOL) {
         targetVersion = ProtocolTranslator.NATIVE_VERSION;
      }

      ((IClientConnection)connection).viaFabricPlus$setTargetVersion(targetVersion);
   }

   @WrapOperation(
      method = "connect(Ljava/net/InetSocketAddress;ZLnet/minecraft/network/ClientConnection;)Lio/netty/channel/ChannelFuture;",
      at = @At(value = "INVOKE", target = "Lio/netty/bootstrap/Bootstrap;channel(Ljava/lang/Class;)Lio/netty/bootstrap/AbstractBootstrap;", remap = false)
   )
   private static AbstractBootstrap<?, ?> useRakNetChannelFactory(
      Bootstrap var0, Class<? extends Channel> var1, Operation<AbstractBootstrap<Bootstrap, Channel>> var2, @Local(argsOnly = true) ClientConnection var3
   ) {
      Object var10000 = null;
      ((IClientConnection)var3).viaFabricPlus$getTargetVersion();
      boolean var4 = false;
      return (AbstractBootstrap<?, ?>)var2.call(new Object[]{var0, var1});
   }

   @Redirect(
      method = "connect(Ljava/net/InetSocketAddress;ZLnet/minecraft/network/ClientConnection;)Lio/netty/channel/ChannelFuture;",
      at = @At(value = "INVOKE", target = "Lio/netty/bootstrap/Bootstrap;connect(Ljava/net/InetAddress;I)Lio/netty/channel/ChannelFuture;", remap = false)
   )
   private static ChannelFuture useRakNetPingHandlers(
      Bootstrap var0, InetAddress var1, int var2, @Local(argsOnly = true) ClientConnection var3, @Local(argsOnly = true) boolean var4
   ) {
      Object var10000 = null;
      ((IClientConnection)var3).viaFabricPlus$getTargetVersion();
      boolean var5 = false;
      return var0.connect(var1, var2);
   }

   @Override
   public void viaFabricPlus$setupPreNettyDecryption() {
      if (this.viaFabricPlus$decryptionCipher == null) {
         throw new IllegalStateException("Decryption cipher is null");
      } else {
         this.encrypted = true;
         this.channel.pipeline().addBefore("vialegacy-pre-netty-length-prepender", "decrypt", new PacketDecryptor(this.viaFabricPlus$decryptionCipher));
      }
   }

   @Override
   public UserConnection viaFabricPlus$getUserConnection() {
      return this.viaFabricPlus$userConnection;
   }

   @Override
   public void viaFabricPlus$setUserConnection(UserConnection connection) {
      this.viaFabricPlus$userConnection = connection;
   }

   @Override
   public ProtocolVersion viaFabricPlus$getTargetVersion() {
      return this.viaFabricPlus$serverVersion;
   }

   @Override
   public void viaFabricPlus$setTargetVersion(ProtocolVersion serverVersion) {
      this.viaFabricPlus$serverVersion = serverVersion;
   }
}
