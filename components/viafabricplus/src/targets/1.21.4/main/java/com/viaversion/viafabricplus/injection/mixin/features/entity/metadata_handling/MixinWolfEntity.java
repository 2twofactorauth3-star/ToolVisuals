package com.viaversion.viafabricplus.injection.mixin.features.entity.metadata_handling;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.world.World;

import com.viaversion.viafabricplus.features.entity.metadata_handling.WolfHealthTracker1_14_4;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WolfEntity.class)
public abstract class MixinWolfEntity extends TameableEntity implements Angerable {

    protected MixinWolfEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/WolfEntity;getHealth()F"))
    private float fixWolfHealth(WolfEntity instance) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_4)) {
            return WolfHealthTracker1_14_4.getWolfHealth(this);
        } else {
            return instance.getHealth();
        }
    }

}
