package ru.he3hauka.toolvisuals.mixin.optimization;

import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.optimization.occlusion.ExtractionVisibility;

@Mixin(ItemModelResolver.class)
public abstract class EarlyEquipmentMixin {
    @Inject(method="updateForLiving",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$skipHiddenEquipment(ItemStackRenderState state,ItemStack item,ItemDisplayContext display,LivingEntity entity,CallbackInfo callback) {
        if(ExtractionVisibility.hidden(entity)){state.clear();callback.cancel();}
    }
}
