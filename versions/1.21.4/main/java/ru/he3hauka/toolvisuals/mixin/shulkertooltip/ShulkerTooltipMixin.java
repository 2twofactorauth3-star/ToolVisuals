package ru.he3hauka.toolvisuals.mixin.shulkertooltip;

import java.util.Optional;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.items.shulkertooltip.ShulkerTooltips;

@Mixin(ItemStack.class)
public abstract class ShulkerTooltipMixin {
    @Inject(method="getTooltipImage",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$container(CallbackInfoReturnable<Optional<TooltipComponent>> callback){var preview=ShulkerTooltips.image((ItemStack)(Object)this);if(preview.isPresent())callback.setReturnValue(Optional.of(preview.get()));}
}
