package com.viaversion.viafabricplus.injection.mixin.features.item.interaction;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;

import com.llamalad7.mixinextras.sugar.Local;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Consumable.class)
public abstract class MixinConsumable {

    @Redirect(method = "startConsuming", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/InteractionResult$Success;heldItemTransformedTo(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/InteractionResult$Success;"))
    private InteractionResult.Success dontExchangeStack(InteractionResult.Success instance, ItemStack newHandStack, @Local(argsOnly = true) ItemStack stack) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_5) && stack.is(Items.MILK_BUCKET)) {
            return instance.heldItemTransformedTo(stack.isEmpty() ? new ItemStack(Items.BUCKET) : stack);
        } else {
            return instance.heldItemTransformedTo(newHandStack);
        }
    }

}
