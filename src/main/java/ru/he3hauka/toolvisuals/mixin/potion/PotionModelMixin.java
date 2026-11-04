package ru.he3hauka.toolvisuals.mixin.potion;

import net.minecraft.client.renderer.item.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.potion.liquid.PotionLiquid;

@Mixin(ItemModelResolver.class)
public abstract class PotionModelMixin {
    @Inject(method="updateForLiving",at=@At("TAIL"))
    private void toolvisuals$potion(ItemStackRenderState state,ItemStack item,ItemDisplayContext context,LivingEntity player,CallbackInfo ci){PotionLiquid.apply(state,item,context,player);}
}
