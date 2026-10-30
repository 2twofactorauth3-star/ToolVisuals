package ru.he3hauka.toolvisuals.mixin.camera;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.ASPECT_RATIO;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.RATIO;

@Mixin(GameRenderer.class)
public abstract class AspectRatioMixin {
    @ModifyArg(method="getProjectionMatrix",at=@At(value="INVOKE",target="Lorg/joml/Matrix4f;perspective(FFFF)Lorg/joml/Matrix4f;"),index=1)
    private float toolvisuals$aspect(float original){return ToolVisualsClient.CONFIG.active(ASPECT_RATIO)?ToolVisualsClient.CONFIG.number(RATIO):original;}
}
