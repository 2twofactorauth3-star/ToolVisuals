package com.viaversion.viafabricplus.injection.mixin.features.sign_editor_reach;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSignEditScreen.class)
public abstract class MixinAbstractSignEditScreen {

    @Shadow
    @Final
    protected SignBlockEntity blockEntity;

    @Inject(method = "canEdit", at = @At("HEAD"), cancellable = true)
    private void dontCloseTooFarAwaySigns(CallbackInfoReturnable<Boolean> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_19_4)) {
            cir.setReturnValue(this.blockEntity.getType().supports(this.blockEntity.getCachedState()));
        }
    }

}
