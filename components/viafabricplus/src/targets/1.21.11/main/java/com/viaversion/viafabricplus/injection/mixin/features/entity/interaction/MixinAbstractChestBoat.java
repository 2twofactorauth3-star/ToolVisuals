package com.viaversion.viafabricplus.injection.mixin.features.entity.interaction;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractChestBoat;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractChestBoat.class)
public abstract class MixinAbstractChestBoat implements ContainerEntity {

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void openWhenSneaking(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21_5) && player.isSecondaryUseActive()) {
            cir.setReturnValue(this.interactWithContainerVehicle(player));
        }
    }

}
