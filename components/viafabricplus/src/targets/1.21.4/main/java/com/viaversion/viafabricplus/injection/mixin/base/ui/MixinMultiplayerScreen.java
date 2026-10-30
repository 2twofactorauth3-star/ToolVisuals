package com.viaversion.viafabricplus.injection.mixin.base.ui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.Builder;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.viaversion.viafabricplus.injection.access.base.IServerInfo;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viafabricplus.settings.impl.GeneralSettings;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.protocols.client.ProtocolMenus;

@Mixin(MultiplayerScreen.class)
public abstract class MixinMultiplayerScreen extends Screen {
   public MixinMultiplayerScreen(Text title) {
      super(title);
   }

   @Inject(method = "init", at = @At("RETURN"))
   private void addProtocolSelectionButton(CallbackInfo ci) {
      int buttonPosition = GeneralSettings.INSTANCE.multiplayerScreenButtonOrientation.getIndex();
      if (buttonPosition != 0) {
         Builder builder = ButtonWidget.builder(Text.of("ViaFabricPlus"), button -> {
            Object var10001 = null;
            ProtocolMenus.open(this);
         }).size(98, 20);
         this.addDrawableChild(GeneralSettings.withOrientation(builder, buttonPosition, this.width, this.height).build());
      }
   }

   @WrapOperation(
      method = "connect(Lnet/minecraft/client/network/ServerInfo;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ServerAddress;parse(Ljava/lang/String;)Lnet/minecraft/client/network/ServerAddress;")
   )
   private ServerAddress replaceDefaultPort(String address, Operation<ServerAddress> original, @Local(argsOnly = true) ServerInfo entry) {
      IServerInfo mixinServerInfo = (IServerInfo)entry;
      if (mixinServerInfo.viaFabricPlus$passedDirectConnectScreen()) {
         ProtocolVersion version = ProtocolTranslator.getTargetVersion();
      } else {
         ProtocolVersion version = mixinServerInfo.viaFabricPlus$forcedVersion();
      }

      return (ServerAddress)original.call(new Object[]{address});
   }

   @WrapOperation(
      method = "directConnect",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;connect(Lnet/minecraft/client/network/ServerInfo;)V")
   )
   private void storeDirectConnectionPhase(MultiplayerScreen instance, ServerInfo entry, Operation<Void> original) {
      ((IServerInfo)entry).viaFabricPlus$passDirectConnectScreen(true);
      original.call(new Object[]{instance, entry});
   }
}
