package ru.he3hauka.toolvisuals.mixin.world;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.world.ambience.Ambience;

@Mixin(FogRenderer.class)
public abstract class AmbienceFogMixin {
    @Inject(method="computeFogColor",at=@At("RETURN"))
    private void toolvisuals$season(CallbackInfo callback,@Local(argsOnly=true) Camera camera,@Local(argsOnly=true) Vector4f color){
        if(camera.getFluidInCamera()==FogType.NONE)Ambience.fog(color);
    }
}
