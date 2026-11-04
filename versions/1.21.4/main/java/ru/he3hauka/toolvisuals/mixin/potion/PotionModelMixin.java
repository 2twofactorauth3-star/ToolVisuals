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
    private void toolvisuals$potion(ItemStackRenderState state,ItemStack item,ItemDisplayContext context,boolean left,LivingEntity player,CallbackInfo ci){
        if(context==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||context==ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)context=left?ItemDisplayContext.FIRST_PERSON_LEFT_HAND:ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        else if(context==ItemDisplayContext.THIRD_PERSON_LEFT_HAND||context==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)context=left?ItemDisplayContext.THIRD_PERSON_LEFT_HAND:ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        PotionLiquid.apply(state,item,context,player);
    }
}
