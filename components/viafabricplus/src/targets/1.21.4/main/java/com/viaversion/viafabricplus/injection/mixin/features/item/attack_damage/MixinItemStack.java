package com.viaversion.viafabricplus.injection.mixin.features.item.attack_damage;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Shadow
    public abstract Item getItem();

    @Redirect(method = "appendAttributeModifierTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAttributeBaseValue(Lnet/minecraft/registry/entry/RegistryEntry;)D", ordinal = 0))
    private double fixAttackDamageCalculation(PlayerEntity instance, RegistryEntry<EntityAttribute> registryEntry) {
        double value = 0.0;
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_5)) {
            final ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments((ItemStack) (Object) this);
            for (RegistryEntry<Enchantment> enchantment : enchantments.getEnchantments()) {
                if (enchantment.matchesKey(Enchantments.SHARPNESS)) {
                    final int level = enchantments.getLevel(enchantment);
                    if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
                        value = level * 1.25F;
                    } else {
                        value = 1.0F + (float) Math.max(0, level - 1) * 0.5F;
                    }
                    break;
                }
            }
        }

        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            return value;
        } else {
            return instance.getAttributeBaseValue(registryEntry) + value;
        }
    }

}
