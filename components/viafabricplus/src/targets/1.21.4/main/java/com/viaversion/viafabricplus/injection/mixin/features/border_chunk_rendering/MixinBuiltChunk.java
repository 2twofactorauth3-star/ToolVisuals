package com.viaversion.viafabricplus.injection.mixin.features.border_chunk_rendering;

import net.minecraft.client.render.chunk.ChunkBuilder;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkBuilder.BuiltChunk.class)
public abstract class MixinBuiltChunk {

    @Inject(method = "shouldBuild", at = @At("HEAD"), cancellable = true)
    private void modifyRenderCondition(CallbackInfoReturnable<Boolean> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            cir.setReturnValue(true);
        }
    }

}
