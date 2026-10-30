package com.viaversion.viafabricplus.features.block.shape;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;


public final class CollisionShapes {

    public static void reloadBlockShapes() {
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block instanceof AnvilBlock || block instanceof BedBlock || block instanceof BrewingStandBlock
                || block instanceof CarpetBlock || block instanceof CauldronBlock || block instanceof ChestBlock
                || block instanceof EnderChestBlock || block instanceof EndPortalBlock || block instanceof EndPortalFrameBlock
                || block instanceof FarmBlock || block instanceof FenceBlock || block instanceof FenceGateBlock
                || block instanceof HopperBlock || block instanceof LadderBlock || block instanceof LeavesBlock
                || block instanceof WaterlilyBlock || block instanceof IronBarsBlock || block instanceof PistonBaseBlock
                || block instanceof PistonHeadBlock || block instanceof SnowLayerBlock || block instanceof WallBlock
                || block instanceof CropBlock || block instanceof FlowerBedBlock
            ) {
                for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                    state.initCache();
                }
            }
        }
    }

}
