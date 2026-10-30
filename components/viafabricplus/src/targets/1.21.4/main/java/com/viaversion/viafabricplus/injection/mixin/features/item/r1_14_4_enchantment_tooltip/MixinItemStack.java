package com.viaversion.viafabricplus.injection.mixin.features.item.r1_14_4_enchantment_tooltip;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import com.viaversion.viafabricplus.features.item.r1_14_4_enchantment_tooltip.Enchantments1_14_4;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viafabricplus.util.ItemUtil;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Shadow
    public abstract Item getItem();

    @Inject(method = "appendTooltip", at = @At("HEAD"), cancellable = true)
    private <T extends TooltipAppender> void replaceEnchantmentTooltip(ComponentType<T> componentType, Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_14_4)) {
            return;
        }

        final NbtCompound tag = ItemUtil.getTagOrNull((ItemStack) (Object) this);
        if (tag == null) {
            return;
        }
        if (componentType == DataComponentTypes.ENCHANTMENTS) {
            this.viaFabricPlus$appendEnchantments1_14_4("Enchantments", tag, context, textConsumer);
            ci.cancel();
        } else if (componentType == DataComponentTypes.STORED_ENCHANTMENTS) {
            this.viaFabricPlus$appendEnchantments1_14_4("StoredEnchantments", tag, context, textConsumer);
            ci.cancel();
        }
    }

    @Unique
    private void viaFabricPlus$appendEnchantments1_14_4(final String name, final NbtCompound nbt, Item.TooltipContext context, final Consumer<Text> tooltip) {
        final RegistryWrapper.WrapperLookup registryLookup = context.getRegistryLookup();
        final NbtList enchantments = nbt.getList(name, NbtElement.COMPOUND_TYPE);
        for (NbtElement element : enchantments) {
            final NbtCompound enchantment = (NbtCompound) element;

            final String id = enchantment.getString("id");
            final Optional<RegistryKey<Enchantment>> value = Enchantments1_14_4.getOrEmpty(id);
            value.ifPresent(e -> {
                final int lvl = enchantment.getInt("lvl");
                if (registryLookup != null) {
                    final Optional<RegistryEntry.Reference<Enchantment>> v = registryLookup.getOrThrow(RegistryKeys.ENCHANTMENT).getOptional(e);
                    v.ifPresent(enchantmentReference -> tooltip.accept(Enchantment.getName(enchantmentReference, MathHelper.clamp(lvl, Short.MIN_VALUE, Short.MAX_VALUE))));
                }
            });
        }
    }

}
