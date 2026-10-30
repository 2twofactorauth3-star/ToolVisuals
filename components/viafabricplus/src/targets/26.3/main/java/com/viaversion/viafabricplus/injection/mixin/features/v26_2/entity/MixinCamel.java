package com.viaversion.viafabricplus.injection.mixin.features.v26_2.entity;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camel.class)
public abstract class MixinCamel {

    @Inject(method = "mobInteract", at = @At(value = "FIELD", target = "Lnet/minecraft/world/InteractionResult;FAIL:Lnet/minecraft/world/InteractionResult$Fail;", opcode = Opcodes.GETSTATIC), cancellable = true)
    private void changeInteraction(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (ViaFabricPlus.api().targetVersion().olderThanOrEqualTo(ProtocolVersion.v26_2)) {
            cir.setReturnValue(InteractionResult.CONSUME);
        }
    }

}
