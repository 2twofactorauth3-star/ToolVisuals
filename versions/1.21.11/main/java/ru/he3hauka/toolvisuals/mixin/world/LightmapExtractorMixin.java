package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.optimization.lighting.FullBrightState;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.FULL_BRIGHT;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.BRIGHTNESS;

@Mixin(LightTexture.class)
public abstract class LightmapExtractorMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow private boolean updateLightTexture;
    @Unique private final FullBrightState toolvisuals$state=new FullBrightState();
    @Inject(method="updateLightTexture",at=@At("HEAD"))
    private void toolvisuals$invalidate(float partial,CallbackInfo callback) {
        if(toolvisuals$state.update(minecraft.level,ToolVisualsClient.CONFIG.active(FULL_BRIGHT),ToolVisualsClient.CONFIG.number(BRIGHTNESS)))updateLightTexture=true;
    }
    @ModifyArg(method="updateLightTexture",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;",ordinal=3),index=0)
    private float toolvisuals$brightness(float original){return ToolVisualsClient.CONFIG.active(FULL_BRIGHT)?ToolVisualsClient.CONFIG.number(BRIGHTNESS):original;}
}
