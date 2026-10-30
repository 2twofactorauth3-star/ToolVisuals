package com.viaversion.viafabricplus.injection.mixin.features.interaction.container_clicking;

import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.BlockItemPacketRewriter1_21_5;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {BlockItemPacketRewriter1_21_5.class, StructuredItemRewriter.class}, remap = false)
public abstract class MixinStructuredItemRewriters {

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lcom/viaversion/viaversion/api/data/entity/EntityTracker;canInstaBuild()Z"))
    private boolean dontCancelPackets(EntityTracker entityTracker) {
        return true;
    }

}
