package com.viaversion.viafabricplus.injection.mixin.features.block.interaction;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.NoteBlock;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public abstract class MixinNoteBlock {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void cancelMobHeadUsage(CallbackInfoReturnable<InteractionResult> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_19_4)) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

}
