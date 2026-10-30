package com.viaversion.viafabricplus.injection.mixin.features.v26_2;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private float destroyTicks;

    @Inject(method = "continueDestroyBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyTicks:F", opcode = Opcodes.GETFIELD))
    private void playBlockHitSound(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {

        if (ViaFabricPlus.api().targetVersion().olderThanOrEqualTo(ProtocolVersion.v26_2) && this.destroyTicks % 4.0F == 0.0F) {
            this.minecraft.level.playBreakingSound(pos, this.minecraft.level.getBlockState(pos));
        }
    }

    @WrapWithCondition(method = "lambda$startDestroyBlock$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;addBreakingBlockEffects(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)V"))
    private boolean dontAddStartBreakingEffects(ClientLevel instance, BlockPos pos, Direction direction, boolean playSound) {

        return ViaFabricPlus.api().targetVersion().newerThanOrEqualTo(ProtocolVersion.v26_3);
    }

    @WrapWithCondition(method = "attack", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyDelay:I"))
    private boolean dontDelayDestroyingAfterAttacking(MultiPlayerGameMode instance, int value) {
        return ViaFabricPlus.api().targetVersion().newerThanOrEqualTo(ProtocolVersion.v26_3);
    }

    @WrapWithCondition(method = "performUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"))
    private boolean dontPredictTransformedItems(LocalPlayer instance, InteractionHand hand, ItemStack itemStack) {

        return ViaFabricPlus.api().targetVersion().newerThanOrEqualTo(ProtocolVersion.v26_3);
    }

}
