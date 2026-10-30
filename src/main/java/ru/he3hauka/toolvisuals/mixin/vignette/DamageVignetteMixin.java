package ru.he3hauka.toolvisuals.mixin.vignette;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.render.vignette.DamageVignette;

@Mixin(Hud.class)
public abstract class DamageVignetteMixin {
    @Inject(method="extractCameraOverlays",at=@At("TAIL"))
    private void toolvisuals$damageVignette(GuiGraphicsExtractor graphics,DeltaTracker delta,CallbackInfo callback) {
        DamageVignette.draw(graphics);
    }
}
