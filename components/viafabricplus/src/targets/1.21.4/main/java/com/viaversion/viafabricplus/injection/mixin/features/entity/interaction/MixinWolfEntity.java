package com.viaversion.viafabricplus.injection.mixin.features.entity.interaction;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import com.viaversion.viafabricplus.features.entity.metadata_handling.WolfHealthTracker1_14_4;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WolfEntity.class)
public abstract class MixinWolfEntity extends TameableEntity implements Angerable {

    @Shadow
    public abstract DyeColor getCollarColor();

    @Shadow
    protected abstract void setCollarColor(DyeColor color);

    protected MixinWolfEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void fixWolfInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_4)) {
            final ItemStack itemStack = player.getStackInHand(hand);
            final Item item = itemStack.getItem();
            if (this.isTamed()) {
                final FoodComponent foodComponent = itemStack.get(DataComponentTypes.FOOD);
                if (foodComponent != null) {
                    if (this.isBreedingItem(itemStack) && WolfHealthTracker1_14_4.getWolfHealth(this) < 20.0F) {
                        if (!player.getAbilities().creativeMode) itemStack.decrement(1);
                        this.heal(foodComponent.nutrition());
                        cir.setReturnValue(ActionResult.SUCCESS);
                        return;
                    }
                } else if (item instanceof DyeItem dyeItem) {
                    final DyeColor dyeColor = dyeItem.getColor();
                    if (dyeColor != this.getCollarColor()) {
                        this.setCollarColor(dyeColor);
                        if (!player.getAbilities().creativeMode) itemStack.decrement(1);
                        cir.setReturnValue(ActionResult.SUCCESS);
                        return;
                    }
                }
            } else if (item == Items.BONE && !this.hasAngerTime()) {
                if (!player.getAbilities().creativeMode) itemStack.decrement(1);
                cir.setReturnValue(ActionResult.SUCCESS);
                return;
            }

            cir.setReturnValue(super.interactMob(player, hand));
        }
    }

}
