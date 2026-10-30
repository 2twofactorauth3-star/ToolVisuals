package com.viaversion.viafabricplus.injection.mixin.base.integration;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.viaversion.viafabricplus.injection.access.base.IServerData;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(JoinMultiplayerScreen.class)
public abstract class MixinJoinMultiplayerScreen extends Screen {
   public MixinJoinMultiplayerScreen(Component component) {
      super(component);
   }

   @WrapOperation(
      method = "join(Lnet/minecraft/client/multiplayer/ServerData;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/multiplayer/resolver/ServerAddress;parseString(Ljava/lang/String;)Lnet/minecraft/client/multiplayer/resolver/ServerAddress;"
      )
   )
   private ServerAddress replaceDefaultPort(String address, Operation<ServerAddress> original, @Local(argsOnly = true) ServerData entry) {
      IServerData mixinServerInfo = (IServerData)entry;
      if (mixinServerInfo.viaFabricPlus$passedDirectConnectScreen()) {
         ProtocolVersion version = ProtocolTranslator.getTargetVersion();
      } else {
         ProtocolVersion version = mixinServerInfo.viaFabricPlus$forcedVersion();
      }

      return (ServerAddress)original.call(new Object[]{address});
   }

   @WrapOperation(
      method = "directJoinCallback",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/screens/multiplayer/JoinMultiplayerScreen;join(Lnet/minecraft/client/multiplayer/ServerData;)V"
      )
   )
   private void storeDirectConnectionPhase(JoinMultiplayerScreen instance, ServerData entry, Operation<Void> original) {
      ((IServerData)entry).viaFabricPlus$passDirectConnectScreen(true);
      original.call(new Object[]{instance, entry});
   }
}
