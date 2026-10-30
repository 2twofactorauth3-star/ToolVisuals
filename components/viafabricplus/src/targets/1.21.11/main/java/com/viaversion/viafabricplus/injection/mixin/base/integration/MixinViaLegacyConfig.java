package com.viaversion.viafabricplus.injection.mixin.base.integration;

import com.viaversion.viafabricplus.settings.impl.GeneralSettings;
import net.raphimc.vialegacy.ViaLegacyConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ViaLegacyConfig.class, remap = false)
public abstract class MixinViaLegacyConfig {

    @Inject(method = {"isLegacySkullLoading", "isLegacySkinLoading"}, at = @At("HEAD"), cancellable = true)
    private void replaceWithVFPSetting(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(GeneralSettings.INSTANCE.loadSkinsAndSkullsInLegacyVersions.getValue());
    }

}
