package com.viaversion.viafabricplus.injection.mixin.features.networking.resource_pack_header;

import net.minecraft.GameVersion;

import com.viaversion.viafabricplus.features.networking.resource_pack_header.ResourcePackHeaderDiff;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(targets = "net.minecraft.client.resource.server.ServerResourcePackLoader$4")
public abstract class MixinServerResourcePackLoader_4 {

    @Redirect(method = "getHeaders", at = @At(value = "INVOKE", target = "Lnet/minecraft/SharedConstants;getGameVersion()Lnet/minecraft/GameVersion;"))
    private GameVersion editHeaders() {
        return ResourcePackHeaderDiff.get(ProtocolTranslator.getTargetVersion());
    }

    @Inject(method = "getHeaders", at = @At("TAIL"), cancellable = true)
    private void removeHeaders(CallbackInfoReturnable<Map<String, String>> cir) {
        final LinkedHashMap<String, String> modifiableMap = new LinkedHashMap<>(cir.getReturnValue());
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_3)) {
            modifiableMap.remove("X-Minecraft-Version-ID");
            if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
                modifiableMap.remove("X-Minecraft-Pack-Format");
                modifiableMap.remove("User-Agent");
            }
        }
        cir.setReturnValue(modifiableMap);
    }

}
