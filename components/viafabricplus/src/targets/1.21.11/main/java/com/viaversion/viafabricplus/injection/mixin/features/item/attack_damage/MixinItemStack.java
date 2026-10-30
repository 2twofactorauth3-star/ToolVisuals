package com.viaversion.viafabricplus.injection.mixin.features.item.attack_damage;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import com.viaversion.viafabricplus.injection.access.item.attack_damage.IDisplayDefault;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Inject(method = "addAttributeTooltips", at = @At("HEAD"))
    private void captureItemEnchantments(Consumer<Component> textConsumer, TooltipDisplay displayComponent, @Nullable Player player, CallbackInfo ci) {
        final ItemStack itemStack = (ItemStack) (Object) this;
        final IDisplayDefault mixinDefault = (IDisplayDefault) ItemAttributeModifiers.Display.attributeModifiers();
        mixinDefault.viaFabricPlus$setItemEnchantments(EnchantmentHelper.getEnchantmentsForCrafting(itemStack));
    }

}
