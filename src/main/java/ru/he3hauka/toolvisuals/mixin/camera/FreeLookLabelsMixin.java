package ru.he3hauka.toolvisuals.mixin.camera;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import ru.he3hauka.toolvisuals.client.camera.FreeLook;

@Mixin(LivingEntityRenderer.class)
public abstract class FreeLookLabelsMixin {
     
    @ModifyExpressionValue(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;"))
    private Entity toolvisuals$freeLookOwnName(Entity original, LivingEntity entity, double distance) {
        return FreeLook.showsOwnLabels(entity) ? null : original;
    }
}
