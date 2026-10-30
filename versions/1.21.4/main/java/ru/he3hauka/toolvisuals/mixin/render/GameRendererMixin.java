package ru.he3hauka.toolvisuals.mixin.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.*;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @ModifyExpressionValue(method="renderLevel",at={@At(value="FIELD",target="Lnet/minecraft/client/player/LocalPlayer;oSpinningEffectIntensity:F"),@At(value="FIELD",target="Lnet/minecraft/client/player/LocalPlayer;spinningEffectIntensity:F")})
    private float toolvisuals$distortion(float intensity){var player=net.minecraft.client.Minecraft.getInstance().player;return NoRender.hides(player!=null&&player.hasEffect(net.minecraft.world.effect.MobEffects.CONFUSION)?NAUSEA:PORTAL_DISTORTION)?0:intensity;}
    @Inject(method="render",at=@At("HEAD")) private void toolvisuals$begin(CallbackInfo callback){ru.he3hauka.toolvisuals.client.optimization.frame.FrameWork.begin();ru.he3hauka.toolvisuals.client.optimization.frame.FrameMonitor.begin();}
    @Inject(method="render",at=@At("RETURN")) private void toolvisuals$end(CallbackInfo callback){ru.he3hauka.toolvisuals.client.optimization.frame.FrameMonitor.end();}
    @ModifyExpressionValue(method="render",at=@At(value="FIELD",target="Lnet/minecraft/client/renderer/GameRenderer;postEffectId:Lnet/minecraft/resources/ResourceLocation;")) private ResourceLocation toolvisuals$postEffect(ResourceLocation effect){return NoRender.hides(POST_EFFECTS)?null:effect;}
    @Inject(method="bobHurt",at=@At("HEAD"),cancellable=true) private void toolvisuals$hurt(CallbackInfo callback){if(NoRender.hides(HURT_CAMERA))callback.cancel();}
    @Inject(method="bobView",at=@At("HEAD"),cancellable=true) private void toolvisuals$bob(CallbackInfo callback){if(NoRender.hides(VIEW_BOBBING))callback.cancel();}
    @Inject(method="renderItemInHand",at=@At("HEAD"),cancellable=true) private void toolvisuals$hands(CallbackInfo callback){if(NoRender.hides(FIRST_PERSON_HANDS))callback.cancel();}
}
