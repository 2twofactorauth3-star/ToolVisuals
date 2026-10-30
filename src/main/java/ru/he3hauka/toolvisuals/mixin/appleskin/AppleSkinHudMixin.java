package ru.he3hauka.toolvisuals.mixin.appleskin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.food.FoodVisuals;

@Mixin(targets="squeek.appleskin.client.HUDOverlayHandler",remap=false)
public abstract class AppleSkinHudMixin {
    @Inject(method="shouldRenderAnyOverlays",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$enabled(CallbackInfoReturnable<Boolean> callback){if(!FoodVisuals.enabled())callback.setReturnValue(false);}

    @Inject(method="onPreRenderFood",at=@At("HEAD"))
    private void toolvisuals$separateRow(GuiGraphicsExtractor graphics, Player player, int y, int x, CallbackInfo callback) {
        FoodVisuals.drawRow(graphics, player, y, x);
    }
}
