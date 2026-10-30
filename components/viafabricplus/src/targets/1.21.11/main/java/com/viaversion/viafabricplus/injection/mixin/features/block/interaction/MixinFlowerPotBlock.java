package com.viaversion.viafabricplus.injection.mixin.features.block.interaction;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;

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
    private Block potted;

    @Inject(method = "useWithoutItem", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/FlowerPotBlock;potted:Lnet/minecraft/world/level/block/Block;", ordinal = 0), cancellable = true)
    private void alwaysConsume(CallbackInfoReturnable<InteractionResult> ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_10) && potted != Blocks.AIR) {
            ci.setReturnValue(InteractionResult.CONSUME);
        }
    }

}
