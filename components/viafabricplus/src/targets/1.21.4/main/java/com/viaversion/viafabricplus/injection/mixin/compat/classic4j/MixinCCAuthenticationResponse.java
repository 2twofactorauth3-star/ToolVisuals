package com.viaversion.viafabricplus.injection.mixin.compat.classic4j;

import net.minecraft.text.Text;

import de.florianmichael.classic4j.model.classicube.CCAuthenticationResponse;
import de.florianmichael.classic4j.model.classicube.CCError;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CCAuthenticationResponse.class, remap = false)
public abstract class MixinCCAuthenticationResponse {

    @Redirect(method = "getErrorDisplay", at = @At(value = "FIELD", target = "Lde/florianmichael/classic4j/model/classicube/CCError;description:Ljava/lang/String;"))
    private String mapTranslations(CCError instance) {
        switch (instance) {
            case TOKEN -> Text.translatable("classic4j_library.viafabricplus.error.token").getString();
            case USERNAME -> Text.translatable("classic4j_library.viafabricplus.error.username").getString();
            case PASSWORD -> Text.translatable("classic4j_library.viafabricplus.error.password").getString();
            case VERIFICATION -> Text.translatable("classic4j_library.viafabricplus.error.verification").getString();
            case LOGIN_CODE -> Text.translatable("classic4j_library.viafabricplus.error.logincode").getString();
        }

        return instance.description;
    }

}
