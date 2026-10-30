package ru.he3hauka.toolvisuals.mixin.world;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.*;

@Mixin(GameRenderer.class)
public abstract class CameraEffectsMixin {
    @ModifyExpressionValue(method="renderLevel",at=@At(value="FIELD",target="Lnet/minecraft/client/player/LocalPlayer;portalEffectIntensity:F"))
    private float toolvisuals$portal(float intensity){return NoRender.hides(PORTAL_DISTORTION)?0:intensity;}
    @ModifyExpressionValue(method="renderLevel",at=@At(value="FIELD",target="Lnet/minecraft/client/player/LocalPlayer;oPortalEffectIntensity:F"))
    private float toolvisuals$oldPortal(float intensity){return NoRender.hides(PORTAL_DISTORTION)?0:intensity;}
    @WrapOperation(method="renderLevel",at=@At(value="INVOKE",target="Lnet/minecraft/client/player/LocalPlayer;getEffectBlendFactor(Lnet/minecraft/core/Holder;F)F"))
    private float toolvisuals$nausea(LocalPlayer player,Holder<MobEffect> effect,float partial,Operation<Float> original){return effect==MobEffects.NAUSEA&&NoRender.hides(NAUSEA)?0:original.call(player,effect,partial);}
    @WrapOperation(method="renderLevel",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/components/DebugScreenOverlay;render3dCrosshair(Lnet/minecraft/client/Camera;)V"))
    private void toolvisuals$crosshair(net.minecraft.client.gui.components.DebugScreenOverlay overlay,net.minecraft.client.Camera camera,Operation<Void> original){if(!NoRender.hides(CROSSHAIR)&&!NoRender.hides(ALL_HUD))original.call(overlay,camera);}
}
