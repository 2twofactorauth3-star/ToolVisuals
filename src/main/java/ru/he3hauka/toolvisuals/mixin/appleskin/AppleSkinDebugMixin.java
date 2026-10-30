package ru.he3hauka.toolvisuals.mixin.appleskin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.food.FoodVisuals;

@Mixin(targets="squeek.appleskin.client.DebugInfoHudEntry",remap=false)
public abstract class AppleSkinDebugMixin {
    @Inject(method="display",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$enabled(CallbackInfo callback){if(!FoodVisuals.enabled())callback.cancel();}
}
