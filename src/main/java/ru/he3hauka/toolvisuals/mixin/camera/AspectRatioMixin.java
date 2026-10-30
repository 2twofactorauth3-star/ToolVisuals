package ru.he3hauka.toolvisuals.mixin.camera;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.ASPECT_RATIO;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.RATIO;

@Mixin(Camera.class)
public abstract class AspectRatioMixin {
    @ModifyArg(method = "createProjectionMatrixForCulling", at = @At(value = "INVOKE",
            target = "Lorg/joml/Matrix4f;perspective(FFFFZ)Lorg/joml/Matrix4f;"), index = 1)
    private float toolvisuals$cullingAspect(float original) {
        return ToolVisualsClient.CONFIG.active(ASPECT_RATIO) ? ToolVisualsClient.CONFIG.number(RATIO) : original;
    }
    @ModifyVariable(method = "setupPerspective", at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private float toolvisuals$aspect(float original, float near, float far, float fov, float width, float height) {
        return ToolVisualsClient.CONFIG.active(ASPECT_RATIO) ? height * ToolVisualsClient.CONFIG.number(RATIO) : original;
    }
}
