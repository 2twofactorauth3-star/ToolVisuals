package com.viaversion.viafabricplus.injection.mixin.features.remove_newer_screen_features;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.class_497$1")
public abstract class MixinStructureBlockScreen_1 extends TextFieldWidget {

    public MixinStructureBlockScreen_1(TextRenderer textRenderer, int x, int y, int width, int height, TextFieldWidget copyFrom, Text text) {
        super(textRenderer, x, y, width, height, copyFrom, text);
    }

    @Inject(method = "charTyped(CI)Z", at = @At("HEAD"), cancellable = true)
    private void removeValidation(char chr, int keyCode, CallbackInfoReturnable<Boolean> ci) {
        if (ViaFabricPlus.getImpl().getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            ci.setReturnValue(super.charTyped(chr, keyCode));
        }
    }

}
