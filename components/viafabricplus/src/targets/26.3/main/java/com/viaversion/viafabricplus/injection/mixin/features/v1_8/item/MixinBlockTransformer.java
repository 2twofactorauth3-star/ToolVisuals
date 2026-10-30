package com.viaversion.viafabricplus.injection.mixin.features.v1_8.item;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockTransformer.class)
public abstract class MixinBlockTransformer {

    @Inject(method = "transformBlock", at = @At("HEAD"), cancellable = true)
    private void preventDirtPathCreation(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (ViaFabricPlus.api().targetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            final Holder<BlockTransformer> transformer = context.getItemInHand().get(DataComponents.BLOCK_TRANSFORMER);
            if (transformer != null && transformer.is(BlockTransformers.SHOVEL)) {
                cir.setReturnValue(InteractionResult.PASS);
            }
        }
    }

    @WrapWithCondition(method = "transformBlock", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private boolean disableItemPlaceSounds(Level instance, Entity except, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch) {
        return ViaFabricPlus.api().targetVersion().newerThan(ProtocolVersion.v1_8);
    }

}
