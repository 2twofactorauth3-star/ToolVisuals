package com.viaversion.viafabricplus.injection.mixin.features.classic.cpe_extension;

import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import com.viaversion.viafabricplus.features.classic.cpe_extension.CPEAdditions;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WeatherEffectRenderer.class)
public abstract class MixinWeatherEffectRenderer {

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
    private boolean forceSnow(List<?> instance) {
        if (CPEAdditions.isSnowing()) {
            return false;
        } else {
            return instance.isEmpty();
        }
    }

    @Inject(method = "getPrecipitationAt", at = @At(value = "HEAD"), cancellable = true)
    private void forceSnow(Level world, BlockPos pos, CallbackInfoReturnable<Biome.Precipitation> cir) {
        if (CPEAdditions.isSnowing()) {
            cir.setReturnValue(Biome.Precipitation.SNOW);
        }
    }

}
