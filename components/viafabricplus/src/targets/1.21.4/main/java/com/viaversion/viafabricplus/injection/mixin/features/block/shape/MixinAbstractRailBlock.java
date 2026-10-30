package com.viaversion.viafabricplus.injection.mixin.features.block.shape;

import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractRailBlock.class)
public abstract class MixinAbstractRailBlock extends Block {

    @Shadow
    @Final
    protected static VoxelShape ASCENDING_SHAPE;

    @Unique
    private static final VoxelShape viaFabricPlus$ascending_shape_r1_10_x = VoxelShapes.fullCube();

    @Unique
    private static final VoxelShape viaFabricPlus$ascending_shape_r1_9_x = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.5D, 16.0D);

    @Unique
    private static final VoxelShape viaFabricPlus$ascending_shape_r1_8_x = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D);

    public MixinAbstractRailBlock(Settings settings) {
        super(settings);
    }

    @Redirect(method = "getOutlineShape", at = @At(value = "FIELD", target = "Lnet/minecraft/block/AbstractRailBlock;ASCENDING_SHAPE:Lnet/minecraft/util/shape/VoxelShape;"))
    private VoxelShape changeOutlineShape() {
        if (ProtocolTranslator.getTargetVersion().equalTo(ProtocolVersion.v1_10)) {
            return viaFabricPlus$ascending_shape_r1_10_x;
        } else if (ProtocolTranslator.getTargetVersion().betweenInclusive(ProtocolVersion.v1_9, ProtocolVersion.v1_9_3)) {
            return viaFabricPlus$ascending_shape_r1_9_x;
        } else if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            return viaFabricPlus$ascending_shape_r1_8_x;
        } else {
            return ASCENDING_SHAPE;
        }
    }

}
