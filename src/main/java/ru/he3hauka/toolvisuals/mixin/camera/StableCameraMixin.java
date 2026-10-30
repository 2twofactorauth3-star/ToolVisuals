package ru.he3hauka.toolvisuals.mixin.camera;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.NO_HURT_CAM;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.CAM_FOV;

@Mixin(Camera.class)
public abstract class StableCameraMixin {
    @Shadow private float fovModifier;
    @Shadow private float oldFovModifier;
    private static boolean toolvisuals$fixedFov() { return ToolVisualsClient.CONFIG.active(NO_HURT_CAM) && ToolVisualsClient.CONFIG.flag(CAM_FOV); }
    @Inject(method = "tickFov", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$stabilizeFov(CallbackInfo ci) {
        if (toolvisuals$fixedFov()) { fovModifier = oldFovModifier = 1; ci.cancel(); }
    }
    @ModifyExpressionValue(method = "calculateFov", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F"))
    private float toolvisuals$immediateStableFov(float original) { return toolvisuals$fixedFov() ? 1 : original; }
}
