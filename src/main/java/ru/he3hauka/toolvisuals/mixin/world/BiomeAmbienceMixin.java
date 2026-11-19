package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.world.ambience.Ambience;

@Mixin(Biome.class)
public abstract class BiomeAmbienceMixin {
    @Inject(method="getGrassColor",at=@At("RETURN"),cancellable=true)
    private void toolvisuals$grass(CallbackInfoReturnable<Integer> callback){callback.setReturnValue(Ambience.vegetation(callback.getReturnValueI(),false));}
    @Inject(method="getFoliageColor",at=@At("RETURN"),cancellable=true)
    private void toolvisuals$leaves(CallbackInfoReturnable<Integer> callback){callback.setReturnValue(Ambience.vegetation(callback.getReturnValueI(),true));}
}
