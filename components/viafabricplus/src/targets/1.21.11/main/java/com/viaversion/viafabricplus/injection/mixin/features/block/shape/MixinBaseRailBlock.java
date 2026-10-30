package com.viaversion.viafabricplus.injection.mixin.features.block.shape;

import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BaseRailBlock.class)
public abstract class MixinBaseRailBlock extends Block {

    @Shadow
    @Final
    private static VoxelShape SHAPE_SLOPE;

    @Unique
    private static final VoxelShape viaFabricPlus$ascending_shape_r1_10_x = Shapes.block();

    @Unique
    private static final VoxelShape viaFabricPlus$ascending_shape_r1_9_x = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.5D, 16.0D);

    @Unique
    private static final VoxelShape viaFabricPlus$ascending_shape_r1_8_x = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D);

    public MixinBaseRailBlock(Properties settings) {
        super(settings);
    }

    @Redirect(method = "getShape", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/BaseRailBlock;SHAPE_SLOPE:Lnet/minecraft/world/phys/shapes/VoxelShape;"))
    private VoxelShape changeOutlineShape() {
        if (ProtocolTranslator.getTargetVersion().equalTo(ProtocolVersion.v1_10)) {
            return viaFabricPlus$ascending_shape_r1_10_x;
        } else if (ProtocolTranslator.getTargetVersion().betweenInclusive(ProtocolVersion.v1_9, ProtocolVersion.v1_9_3)) {
            return viaFabricPlus$ascending_shape_r1_9_x;
        } else if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            return viaFabricPlus$ascending_shape_r1_8_x;
        } else {
            return SHAPE_SLOPE;
        }
    }

}
