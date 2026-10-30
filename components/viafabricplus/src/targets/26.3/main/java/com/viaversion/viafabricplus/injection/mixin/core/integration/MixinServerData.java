package com.viaversion.viafabricplus.injection.mixin.core.integration;

import com.llamalad7.mixinextras.sugar.Local;
import com.viaversion.viafabricplus.injection.access.core.IServerData;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslationImpl;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.connection.ProtocolPolicy;

@Mixin(ServerData.class)
public abstract class MixinServerData implements IServerData {
   @Shadow
   public String name;
   @Unique
   private ProtocolVersion viaFabricPlus$forcedVersion = ProtocolPolicy.normalizeNullable(null, ProtocolTranslationImpl.NATIVE_VERSION);
   @Unique
   private boolean viaFabricPlus$passedDirectConnectScreen;
   @Unique
   private ProtocolVersion viaFabricPlus$translatingVersion;

   @Inject(method = "write", at = @At("TAIL"))
   private void saveForcedVersion(CallbackInfoReturnable<CompoundTag> cir, @Local(name = "tag") CompoundTag tag) {
      if (this.viaFabricPlus$forcedVersion != null) {
         tag.putString("viafabricplus_forcedversion", this.viaFabricPlus$forcedVersion.getName());
      }
   }

   @Inject(method = "read", at = @At("TAIL"))
   private static void loadForcedVersion(CompoundTag tag, CallbackInfoReturnable<ServerData> cir, @Local(name = "server") ServerData server) {
      if (tag.contains("viafabricplus_forcedversion")) {
         ProtocolVersion version = ProtocolVersion.getClosest(tag.getStringOr("viafabricplus_forcedversion", ""));
         if (version != null) {
            ((IServerData)server).viaFabricPlus$forceVersion(version);
         }
      }
   }

   @Inject(method = "copyNameIconFrom", at = @At("RETURN"))
   private void syncForcedVersion(ServerData other, CallbackInfo ci) {
      this.viaFabricPlus$forceVersion(((IServerData)other).viaFabricPlus$forcedVersion());
   }

   @Override
   public ProtocolVersion viaFabricPlus$forcedVersion() {
      return this.viaFabricPlus$forcedVersion;
   }

   @Override
   public void viaFabricPlus$forceVersion(ProtocolVersion version) {
      this.viaFabricPlus$forcedVersion = ProtocolPolicy.normalizeNullable(version, ProtocolTranslationImpl.NATIVE_VERSION);
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
