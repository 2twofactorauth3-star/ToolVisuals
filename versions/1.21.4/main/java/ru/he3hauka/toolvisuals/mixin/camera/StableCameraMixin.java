package ru.he3hauka.toolvisuals.mixin.camera;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.NO_HURT_CAM;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.CAM_FOV;

@Mixin(GameRenderer.class)
public abstract class StableCameraMixin {
    @Shadow private float fovModifier;
    @Shadow private float oldFovModifier;
    private static boolean toolvisuals$fixed(){return ToolVisualsClient.CONFIG.active(NO_HURT_CAM)&&ToolVisualsClient.CONFIG.flag(CAM_FOV);}
    @Inject(method="tickFov",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$stable(CallbackInfo callback){if(toolvisuals$fixed()){fovModifier=oldFovModifier=1;callback.cancel();}}
    @ModifyExpressionValue(method="getFov",at=@At(value="INVOKE",target="Lnet/minecraft/util/Mth;lerp(FFF)F"))
    private float toolvisuals$instant(float value){return toolvisuals$fixed()?1:value;}
}
