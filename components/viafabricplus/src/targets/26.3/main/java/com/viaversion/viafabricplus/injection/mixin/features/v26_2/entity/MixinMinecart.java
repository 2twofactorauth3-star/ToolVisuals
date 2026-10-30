package com.viaversion.viafabricplus.injection.mixin.features.v26_2.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecart.class)
public abstract class MixinMinecart {

    @ModifyExpressionValue(method = "interact", at = @At(value = "FIELD", target = "Lnet/minecraft/world/InteractionResult;CONSUME:Lnet/minecraft/world/InteractionResult$Success;", opcode = Opcodes.GETSTATIC))
    private InteractionResult.Success swingHand(InteractionResult.Success original) {
        if (ViaFabricPlus.api().targetVersion().olderThanOrEqualTo(ProtocolVersion.v26_2)) {
            return InteractionResult.SUCCESS;
        } else {
            return original;
        }
    }

}
