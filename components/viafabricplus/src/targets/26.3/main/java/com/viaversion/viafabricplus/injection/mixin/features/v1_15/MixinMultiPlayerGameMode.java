package com.viaversion.viafabricplus.injection.mixin.features.v1_15;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.SwingAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode {

    @WrapOperation(method = "dropItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;Z)Z"))
    private boolean disableSwing(LocalPlayer instance, InteractionHand hand, SwingAnimation animation, boolean sendToSwingingEntity, Operation<Boolean> original) {
        if (ViaFabricPlus.api().targetVersion().newerThanOrEqualTo(ProtocolVersion.v1_15)) {
            return original.call(instance, hand, animation, sendToSwingingEntity);
        } else {
            return false;
        }
    }

}
