package com.viaversion.viafabricplus.injection.mixin.features.block.interaction;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.util.ActionResult;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowerPotBlock.class)
public abstract class MixinFlowerPotBlock {

    @Shadow
    @Final
    private Block content;

    @Inject(method = "onUse", at = @At(value = "FIELD", target = "Lnet/minecraft/block/FlowerPotBlock;content:Lnet/minecraft/block/Block;", ordinal = 0), cancellable = true)
    private void alwaysConsume(CallbackInfoReturnable<ActionResult> ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_10) && content != Blocks.AIR) {
            ci.setReturnValue(ActionResult.CONSUME);
        }
    }

}
