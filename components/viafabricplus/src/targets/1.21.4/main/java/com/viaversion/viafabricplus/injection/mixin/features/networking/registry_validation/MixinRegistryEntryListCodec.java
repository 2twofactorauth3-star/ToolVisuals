package com.viaversion.viafabricplus.injection.mixin.features.networking.registry_validation;

import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryListCodec;
import net.minecraft.registry.tag.TagKey;

import com.mojang.serialization.DataResult;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RegistryEntryListCodec.class)
public abstract class MixinRegistryEntryListCodec {

    @Inject(method = "get", at = @At("RETURN"), cancellable = true)
    private static void workaroundValidation(RegistryEntryLookup registry, TagKey tag, CallbackInfoReturnable<DataResult<RegistryEntryList>> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21)) {

            if (cir.getReturnValue().isError()) {
                cir.setReturnValue(DataResult.success(RegistryEntryList.empty()));
            }
        }
    }

}
