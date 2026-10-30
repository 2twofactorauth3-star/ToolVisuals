package com.viaversion.viafabricplus.injection.mixin.features.legacy_tab_completion;

import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractCommandBlockScreen.class)
public abstract class MixinAbstractCommandBlockScreen {

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ChatInputSuggestor;refresh()V"))
    private void cancelAutoComplete(ChatInputSuggestor instance) {
        if (ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_13)) {
            instance.refresh();
        }
    }

}
