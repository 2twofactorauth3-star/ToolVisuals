package com.viaversion.viafabricplus.injection.mixin.features.networking.srv_resolving;

import net.minecraft.client.network.RedirectResolver;
import net.minecraft.client.network.ServerAddress;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.naming.directory.DirContext;
import java.util.Optional;

@Mixin(RedirectResolver.class)
public interface MixinRedirectResolver {

    @Inject(method = "method_36911", at = @At("HEAD"), cancellable = true)
    private static void disableSrvForPre1_3(DirContext context, ServerAddress address, CallbackInfoReturnable<Optional<ServerAddress>> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThan(LegacyProtocolVersion.r1_3_1tor1_3_2)) {
            cir.setReturnValue(Optional.empty());
        }
    }

}
