package com.viaversion.viafabricplus.visuals.injection.mixin.force_unicode_font;

import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.resource.ResourceManager;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viafabricplus.visuals.features.force_unicode_font.UnicodeFontFix1_12_2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LanguageManager.class)
public abstract class MixinLanguageManager {

    @Inject(method = "reload", at = @At("RETURN"))
    private void updateUnicodeFontOverride(ResourceManager manager, CallbackInfo ci) {
        UnicodeFontFix1_12_2.updateUnicodeFontOverride(ViaFabricPlus.getImpl().getTargetVersion());
    }

}
