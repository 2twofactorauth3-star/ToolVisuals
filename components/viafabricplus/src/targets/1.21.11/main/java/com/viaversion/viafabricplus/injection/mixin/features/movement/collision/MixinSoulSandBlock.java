package com.viaversion.viafabricplus.injection.mixin.features.movement.collision;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoulSandBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SoulSandBlock.class)
public abstract class MixinSoulSandBlock extends Block {

    public MixinSoulSandBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected void entityInside(final BlockState state, final Level world, final BlockPos pos, final Entity entity, final InsideBlockEffectApplier handler, final boolean bl) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_4)) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.4D, 1, 0.4D));
        }
    }

    @Override
    public float getSpeedFactor() {
        return ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_4) ? 1F : super.getSpeedFactor();
    }

}
