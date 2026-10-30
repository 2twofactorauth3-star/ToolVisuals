package ru.he3hauka.toolvisuals.mixin.appleskin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.food.FoodVisuals;

@Mixin(targets="squeek.appleskin.client.TooltipOverlayHandler",remap=false)
public abstract class AppleSkinTooltipMixin {
    @Inject(method="shouldShowTooltip",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$enabled(CallbackInfoReturnable<Boolean> callback){if(!FoodVisuals.tooltips())callback.setReturnValue(false);}
}
