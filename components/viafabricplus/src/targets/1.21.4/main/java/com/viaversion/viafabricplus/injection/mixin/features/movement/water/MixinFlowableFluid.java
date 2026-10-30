package com.viaversion.viafabricplus.injection.mixin.features.movement.water;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viafabricplus.protocoltranslator.impl.ViaFabricPlusMappingDataLoader;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FlowableFluid.class)
public abstract class MixinFlowableFluid {

    @Redirect(method = "isFlowBlocked", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isSideSolidFullSquare(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z"))
    private boolean modifyIsSolidBlock(BlockState instance, BlockView blockView, BlockPos blockPos, Direction direction) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_11_1)) {
            final ViaFabricPlusMappingDataLoader.Material material = ViaFabricPlusMappingDataLoader.MATERIALS.get(ViaFabricPlusMappingDataLoader.getBlockMaterial(instance.getBlock()));
            return material.solid();
        } else if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
            final Block block = instance.getBlock();
            if (block instanceof ShulkerBoxBlock || block instanceof LeavesBlock || block instanceof TrapdoorBlock ||
                    block == Blocks.BEACON || block == Blocks.CAULDRON || block == Blocks.GLASS ||
                    block == Blocks.GLOWSTONE || block == Blocks.ICE || block == Blocks.SEA_LANTERN ||
                    block instanceof StainedGlassBlock || block == Blocks.PISTON || block == Blocks.STICKY_PISTON ||
                    block == Blocks.PISTON_HEAD || block instanceof StairsBlock) {
                return false;
            }
        }
        return instance.isSideSolidFullSquare(blockView, blockPos, direction);
    }

}
