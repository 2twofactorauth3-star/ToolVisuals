package com.viaversion.viafabricplus.injection.mixin.features.legacy_tab_completion;

import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.inventory.AbstractCommandBlockEditScreen;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractCommandBlockEditScreen.class)
public abstract class MixinAbstractCommandBlockEditScreen {

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/CommandSuggestions;updateCommandInfo()V"))
    private void cancelAutoComplete(CommandSuggestions instance) {
        if (ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_13)) {
            instance.updateCommandInfo();
        }
    }

}
