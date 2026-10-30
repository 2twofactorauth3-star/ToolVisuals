package com.viaversion.viafabricplus.injection.mixin.features.swinging;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @WrapWithCondition(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;swingHand(Lnet/minecraft/util/Hand;)V"))
    private boolean disableSwing(ClientPlayerEntity instance, Hand hand) {
        return ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_15);
    }

    @Redirect(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ActionResult$Success;swingSource()Lnet/minecraft/util/ActionResult$SwingSource;", ordinal = 0))
    private ActionResult.SwingSource disableSwing(ActionResult.Success instance) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_4)) {
            return ActionResult.SwingSource.NONE;
        } else {
            return instance.swingSource();
        }
    }

    @Redirect(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ActionResult$Success;swingSource()Lnet/minecraft/util/ActionResult$SwingSource;", ordinal = 2))
    private ActionResult.SwingSource disableSwing2(ActionResult.Success instance) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_4)) {
            return ActionResult.SwingSource.NONE;
        } else {
            return instance.swingSource();
        }
    }

    @Inject(method = "doAttack", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;crosshairTarget:Lnet/minecraft/util/hit/HitResult;", ordinal = 0))
    private void fixSwingPacketOrder(CallbackInfoReturnable<Boolean> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            this.player.swingHand(Hand.MAIN_HAND);
        }
    }

    @WrapWithCondition(method = "doAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;swingHand(Lnet/minecraft/util/Hand;)V"))
    private boolean fixSwingPacketOrder(ClientPlayerEntity instance, Hand hand) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_8);
    }

}
