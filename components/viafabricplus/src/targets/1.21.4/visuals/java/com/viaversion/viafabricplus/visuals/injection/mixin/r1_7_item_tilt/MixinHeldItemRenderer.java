package com.viaversion.viafabricplus.visuals.injection.mixin.r1_7_item_tilt;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class MixinHeldItemRenderer {

    @Shadow
    protected abstract void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress);

    @Inject(method = "renderFirstPersonItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V", ordinal = 2, shift = At.Shift.AFTER))
    private void applyFoodSwingOffset(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        viaFabricPlusVisuals$applySwingOffset(player, hand, swingProgress, matrices);
    }

    @Inject(method = "renderFirstPersonItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V", ordinal = 3, shift = At.Shift.AFTER))
    private void applyBlockingSwingOffset(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        viaFabricPlusVisuals$applySwingOffset(player, hand, swingProgress, matrices);
    }

    @Inject(method = "renderFirstPersonItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V", ordinal = 4, shift = At.Shift.AFTER))
    private void applyBowSwingOffset(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        viaFabricPlusVisuals$applySwingOffset(player, hand, swingProgress, matrices);
    }

    @Inject(method = "renderFirstPersonItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal = 1))
    private void slightlyTiltItemPosition(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack stack, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (VisualSettings.INSTANCE.tiltItemPositions.isEnabled() && !(stack.getItem() instanceof BlockItem)) {
            final Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
            final int direction = arm == Arm.RIGHT ? 1 : -1;

            final float scale = 0.7585F / 0.86F;
            matrices.scale(scale, scale, scale);
            matrices.translate(direction * -0.084F, 0.059F, 0.08F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * 5.0F));
        }
    }

    @Unique
    private void viaFabricPlusVisuals$applySwingOffset(AbstractClientPlayerEntity player, Hand hand, float swingProgress, MatrixStack matrices) {
        if (VisualSettings.INSTANCE.swingHandOnItemUse.isEnabled()) {
            final Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
            applySwingOffset(matrices, arm, swingProgress);
        }
    }

}
