package com.viaversion.viafabricplus.injection.mixin.features.v26_2.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.ShelfBlock;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShelfBlock.class)
public abstract class MixinShelfBlock {

    @ModifyExpressionValue(method = "useItemOn", at = @At(value = "FIELD", target = "Lnet/minecraft/world/InteractionResult;CONSUME:Lnet/minecraft/world/InteractionResult$Success;", opcode = Opcodes.GETSTATIC, ordinal = 0))
    private InteractionResult.Success swingHand(InteractionResult.Success original) {
        if (ViaFabricPlus.api().targetVersion().olderThanOrEqualTo(ProtocolVersion.v26_2)) {
            return InteractionResult.SUCCESS;
        } else {
            return original;
        }
    }

}
