package ru.he3hauka.toolvisuals.mixin.optimization;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import ru.he3hauka.toolvisuals.client.optimization.equipment.ItemTurns;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.OPT_FAST_VISUALS;

@Mixin(ItemInHandLayer.class)
public abstract class OptimizationItemTurnsMixin {
    @WrapOperation(method = "submitArmWithItem", at = @At(value = "INVOKE", target =
            "Lcom/mojang/math/Axis;rotationDegrees(F)Lorg/joml/Quaternionf;"))
    private Quaternionf toolvisuals$reuseTurn(Axis axis, float degrees, Operation<Quaternionf> original) {
        if (!Optimizations.enabled(OPT_FAST_VISUALS)) return original.call(axis, degrees);
        Quaternionf rotation = ItemTurns.degrees(axis, degrees);
        return rotation == null ? original.call(axis, degrees) : rotation;
    }
}
