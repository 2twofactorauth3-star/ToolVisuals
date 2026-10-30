package com.viaversion.viafabricplus.injection.mixin.features.limitation.book_edit;

import net.minecraft.client.gui.screens.inventory.BookEditScreen;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BookEditScreen.class)
public abstract class MixinBookEditScreen {

    @ModifyConstant(method = "init", constant = @Constant(intValue = 1024))
    private int modifyPageLength(int constant) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
            return 256;
        } else {
            return constant;
        }
    }

    @ModifyConstant(method = "appendPageToBook", constant = @Constant(intValue = 100))
    private int modifyPageCount(int constant) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
            return 50;
        } else {
            return constant;
        }
    }

}
