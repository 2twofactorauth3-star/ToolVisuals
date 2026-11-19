package ru.he3hauka.toolvisuals.mixin.world;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.world.ambience.Ambience;

@Mixin(FogRenderer.class)
public abstract class AmbienceFogMixin {
    @Inject(method="computeFogColor",at=@At("RETURN"))
    private static void toolvisuals$season(CallbackInfoReturnable<Vector4f> callback,@Local(argsOnly=true) Camera camera){
        if(camera.getFluidInCamera()==FogType.NONE)Ambience.fog(callback.getReturnValue());
    }
}
