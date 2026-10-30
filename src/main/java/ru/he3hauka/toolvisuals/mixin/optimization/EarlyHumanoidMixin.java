package ru.he3hauka.toolvisuals.mixin.optimization;

import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.optimization.occlusion.ExtractionVisibility;

@Mixin(HumanoidMobRenderer.class)
public abstract class EarlyHumanoidMixin {
    @Inject(method="extractHumanoidRenderState",at=@At("HEAD"),cancellable=true)
    private static void toolvisuals$skipHiddenEquipment(LivingEntity entity,HumanoidRenderState state,float partial,ItemModelResolver resolver,CallbackInfo callback) {
        if(ExtractionVisibility.hidden(entity))callback.cancel();
    }
}
