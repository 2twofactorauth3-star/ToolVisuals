package com.viaversion.viafabricplus.injection.mixin.features.networking.registry_validation;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RegistryEntryList.Named.class)
public abstract class MixinRegistryEntryList_Named {

    @Shadow
    private @Nullable List<RegistryEntry> entries;

    @Shadow
    abstract void setEntries(List<RegistryEntry> entries);

    @Inject(method = "getEntries", at = @At("HEAD"))
    private void preventNullableEntries(CallbackInfoReturnable<List<RegistryEntry<?>>> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21)) {

            if (this.entries == null) {
                this.setEntries(List.of());
            }
        }
    }

    @Inject(method = "isBound", at = @At("HEAD"), cancellable = true)
    private void alwaysBound(CallbackInfoReturnable<Boolean> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21)) {

            cir.setReturnValue(true);
        }
    }

}
