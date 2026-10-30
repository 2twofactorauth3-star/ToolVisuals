package com.viaversion.viafabricplus.features.entity.attribute;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import java.util.Optional;

public final class EnchantmentAttributesEmulation1_20_6 {

    static {
        ClientTickEvents.START_WORLD_TICK.register(world -> {
            if (ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_20_5)) {
                return;
            }

            for (Entity entity : world.getEntities()) {
                if (entity.isLogicalSideForUpdatingMovement() && entity instanceof LivingEntity livingEntity) {
                    livingEntity.getAttributeInstance(EntityAttributes.WATER_MOVEMENT_EFFICIENCY).setBaseValue(getEquipmentLevel(Enchantments.DEPTH_STRIDER, livingEntity) / 3F);
                    setGenericMovementEfficiencyAttribute(livingEntity);
                }
            }

            for (PlayerEntity player : world.getPlayers()) {
                if (!player.isLogicalSideForUpdatingMovement()) {
                    continue;
                }
                final int efficiencyLevel = getEquipmentLevel(Enchantments.EFFICIENCY, player);
                if (efficiencyLevel > 0) {
                    player.getAttributeInstance(EntityAttributes.MINING_EFFICIENCY).setBaseValue(efficiencyLevel * efficiencyLevel + 1);
                } else {
                    player.getAttributeInstance(EntityAttributes.MINING_EFFICIENCY).setBaseValue(0);
                }

                player.getAttributeInstance(EntityAttributes.SNEAKING_SPEED).setBaseValue(0.3F + getEquipmentLevel(Enchantments.SWIFT_SNEAK, player) * 0.15F);
                player.getAttributeInstance(EntityAttributes.SUBMERGED_MINING_SPEED).setBaseValue(getEquipmentLevel(Enchantments.AQUA_AFFINITY, player) <= 0 ? 0.2F : 1F);
            }
        });
    }

    public static void init() {

    }

    public static void setGenericMovementEfficiencyAttribute(final LivingEntity entity) {
        final boolean isOnSoulSpeedBlock = entity.getWorld().getBlockState(entity.getVelocityAffectingPos()).isIn(BlockTags.SOUL_SPEED_BLOCKS);
        if (isOnSoulSpeedBlock && getEquipmentLevel(Enchantments.SOUL_SPEED, entity) > 0) {
            entity.getAttributeInstance(EntityAttributes.MOVEMENT_EFFICIENCY).setBaseValue(1);
        } else {
            entity.getAttributeInstance(EntityAttributes.MOVEMENT_EFFICIENCY).setBaseValue(0);
        }
    }

    private static int getEquipmentLevel(final RegistryKey<Enchantment> enchantment, final LivingEntity entity) {
        final Optional<RegistryEntry.Reference<Enchantment>> enchantmentRef = entity.getWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOptional(enchantment);
        return enchantmentRef.map(e -> EnchantmentHelper.getEquipmentLevel(e, entity)).orElse(0);
    }

}
