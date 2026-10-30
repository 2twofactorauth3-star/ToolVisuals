package ru.he3hauka.toolvisuals.mixin.optimization;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.he3hauka.toolvisuals.client.optimization.equipment.PreparedEquipment;
import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.OPT_FAST_VISUALS;

@Mixin(HumanoidMobRenderer.class)
public abstract class EquipmentSnapshotMixin {
    @WrapOperation(method="getEquipmentIfRenderable",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack toolvisuals$stableEquipment(ItemStack source,Operation<ItemStack> original) {
        return Optimizations.enabled(OPT_FAST_VISUALS) ? PreparedEquipment.copy(source) : original.call(source);
    }
}
