package com.viaversion.viafabricplus.injection.mixin.base.integration;

import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;

import com.llamalad7.mixinextras.sugar.Local;
import com.viaversion.viafabricplus.injection.access.base.IServerInfo;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viafabricplus.save.impl.SettingsSave;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.connection.ProtocolPolicy;

@Mixin(ServerInfo.class)
public abstract class MixinServerInfo implements IServerInfo {
   @Shadow
   public String name;
   @Unique
   private ProtocolVersion viaFabricPlus$forcedVersion = ProtocolPolicy.normalizeNullable(null, ProtocolTranslator.NATIVE_VERSION);
   @Unique
   private boolean viaFabricPlus$passedDirectConnectScreen;
   @Unique
   private ProtocolVersion viaFabricPlus$translatingVersion;

   @Inject(method = "toNbt", at = @At("TAIL"))
   private void saveForcedVersion(CallbackInfoReturnable<NbtCompound> cir, @Local NbtCompound nbtCompound) {
      if (this.viaFabricPlus$forcedVersion != null) {
         nbtCompound.putString("viafabricplus_forcedversion", this.viaFabricPlus$forcedVersion.getName());
      }
   }

   @Inject(method = "fromNbt", at = @At("TAIL"))
   private static void loadForcedVersion(NbtCompound root, CallbackInfoReturnable<ServerInfo> cir, @Local ServerInfo serverInfo) {
      if (root.contains("viafabricplus_forcedversion")) {
         ProtocolVersion version = SettingsSave.protocolVersionByName(root.getString("viafabricplus_forcedversion"));
         if (version != null) {
            ((IServerInfo)serverInfo).viaFabricPlus$forceVersion(version);
         }
      }
   }

   @Inject(method = "copyFrom", at = @At("RETURN"))
   private void syncForcedVersion(ServerInfo serverInfo, CallbackInfo ci) {
      this.viaFabricPlus$forceVersion(((IServerInfo)serverInfo).viaFabricPlus$forcedVersion());
   }

   @Override
   public ProtocolVersion viaFabricPlus$forcedVersion() {
      return this.viaFabricPlus$forcedVersion;
   }

   @Override
   public void viaFabricPlus$forceVersion(ProtocolVersion version) {
      this.viaFabricPlus$forcedVersion = ProtocolPolicy.normalizeNullable(version, ProtocolTranslator.NATIVE_VERSION);
   }

   @Override
   public boolean viaFabricPlus$passedDirectConnectScreen() {
      return this.viaFabricPlus$passedDirectConnectScreen;
   }

   @Override
   public void viaFabricPlus$passDirectConnectScreen(boolean state) {
      this.viaFabricPlus$passedDirectConnectScreen = state;
   }

   @Override
   public ProtocolVersion viaFabricPlus$translatingVersion() {
      return this.viaFabricPlus$translatingVersion;
   }

   @Override
   public void viaFabricPlus$setTranslatingVersion(ProtocolVersion version) {
      this.viaFabricPlus$translatingVersion = version;
   }
}
