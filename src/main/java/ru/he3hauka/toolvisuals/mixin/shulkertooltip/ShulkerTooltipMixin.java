package ru.he3hauka.toolvisuals.mixin.shulkertooltip;

import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.items.shulkertooltip.ShulkerPreview;
import ru.he3hauka.toolvisuals.client.items.shulkertooltip.ShulkerTooltips;

@Mixin(ItemStack.class)
public abstract class ShulkerTooltipMixin {
    @Inject(method = "getTooltipImage", at = @At("RETURN"), cancellable = true)
    private void toolvisuals$shulkerImage(CallbackInfoReturnable<Optional<TooltipComponent>> callback) {
        if (callback.getReturnValue().isPresent()) return;
        ShulkerTooltips.image((ItemStack)(Object)this).ifPresent(preview -> callback.setReturnValue(Optional.of(preview)));
    }

    @Inject(method = "addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/component/TooltipProvider$Getter;Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/item/component/TooltipDisplay;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$replaceContentsText(DataComponentType<?> type, TooltipProvider.Getter<?> provider, Item.TooltipContext context,
            TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag, CallbackInfo callback) {
        if (type != DataComponents.CONTAINER) return;
        var stack = (ItemStack)(Object)this;
        if (ShulkerTooltips.image(stack).isPresent() && stack.getTooltipImage().orElse(null) instanceof ShulkerPreview) callback.cancel();
    }
}
