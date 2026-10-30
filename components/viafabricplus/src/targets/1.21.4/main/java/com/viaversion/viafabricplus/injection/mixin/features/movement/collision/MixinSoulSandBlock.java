package com.viaversion.viafabricplus.injection.mixin.features.movement.collision;

import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoulSandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SoulSandBlock.class)
public abstract class MixinSoulSandBlock extends Block {

    public MixinSoulSandBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_4)) {
            entity.setVelocity(entity.getVelocity().multiply(0.4D, 1, 0.4D));
        }
    }

    @Override
    public float getVelocityMultiplier() {
        return ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_4) ? 1F : super.getVelocityMultiplier();
    }

}
