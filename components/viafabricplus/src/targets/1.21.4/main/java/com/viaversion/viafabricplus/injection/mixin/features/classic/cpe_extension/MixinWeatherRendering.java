package com.viaversion.viafabricplus.injection.mixin.features.classic.cpe_extension;

import net.minecraft.client.render.WeatherRendering;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import com.viaversion.viafabricplus.features.classic.cpe_extension.CPEAdditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WeatherRendering.class)
public abstract class MixinWeatherRendering {

    @Redirect(method = "renderPrecipitation(Lnet/minecraft/world/World;Lnet/minecraft/client/render/VertexConsumerProvider;IFLnet/minecraft/util/math/Vec3d;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRainGradient(F)F"))
    private float forceSnow(World instance, float delta) {
        if (CPEAdditions.isSnowing()) {
            return 1F;
        } else {
            return instance.getRainGradient(delta);
        }
    }

    @Inject(method = "getPrecipitationAt", at = @At(value = "HEAD"), cancellable = true)
    private void forceSnow(World world, BlockPos pos, CallbackInfoReturnable<Biome.Precipitation> cir) {
        if (CPEAdditions.isSnowing()) {
            cir.setReturnValue(Biome.Precipitation.SNOW);
        }
    }

}
