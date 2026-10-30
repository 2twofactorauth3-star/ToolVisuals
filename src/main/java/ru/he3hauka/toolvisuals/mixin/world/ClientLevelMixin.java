package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.render.filter.NoRender;

import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.LIGHTNING_FLASH;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Inject(method = "tickWeatherEffects", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$clearWeather(CallbackInfo ci) {
        var config = ru.he3hauka.toolvisuals.client.ToolVisualsClient.CONFIG;
        if (config.active(ru.he3hauka.toolvisuals.client.module.VisualModule.WORLD_CUSTOMIZER)
                && config.flag(ru.he3hauka.toolvisuals.client.module.VisualSetting.CLEAR_WEATHER)) ci.cancel();
    }
    @Inject(method = "getSkyFlashTime", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$hideLightningFlash(CallbackInfoReturnable<Integer> cir) {
        if (NoRender.hides(LIGHTNING_FLASH)) cir.setReturnValue(0);
    }
}

