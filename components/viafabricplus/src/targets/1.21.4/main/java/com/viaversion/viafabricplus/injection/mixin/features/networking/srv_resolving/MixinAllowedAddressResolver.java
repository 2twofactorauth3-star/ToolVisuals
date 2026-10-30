package com.viaversion.viafabricplus.injection.mixin.features.networking.srv_resolving;

import net.minecraft.client.network.Address;
import net.minecraft.client.network.AddressResolver;
import net.minecraft.client.network.AllowedAddressResolver;
import net.minecraft.client.network.ServerAddress;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.Optional;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AllowedAddressResolver.class)
public abstract class MixinAllowedAddressResolver {
   @Shadow
   @Final
   private AddressResolver addressResolver;

   @Inject(method = "resolve", at = @At("HEAD"), cancellable = true)
   private void oldResolveBehaviour(ServerAddress var1, CallbackInfoReturnable<Optional<Address>> var2) {
      if (!ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_16_4)) {
         ProtocolTranslator.getTargetVersion();
         Object var10001 = null;
         boolean var10000 = false;
      } else {
         var2.setReturnValue(this.addressResolver.resolve(var1));
      }
   }
}
