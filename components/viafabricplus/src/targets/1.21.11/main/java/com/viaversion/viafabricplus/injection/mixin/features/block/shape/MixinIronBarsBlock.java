package com.viaversion.viafabricplus.injection.mixin.features.block.shape;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.viaversion.viafabricplus.injection.access.block.shape.ICrossCollisionBlock;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viafabricplus.settings.impl.DebugSettings;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IronBarsBlock.class)
public abstract class MixinIronBarsBlock extends CrossCollisionBlock implements ICrossCollisionBlock {

    @Unique
    private VoxelShape[] viaFabricPlus$shape_r1_12_2;

    @Unique
    private VoxelShape[] viaFabricPlus$shape_r1_8;

    protected MixinIronBarsBlock(float radius1, float radius2, float boundingHeight1, float boundingHeight2, float collisionHeight, Properties settings) {
        super(radius1, radius2, boundingHeight1, boundingHeight2, collisionHeight, settings);
    }

    @Inject(method = "getVisualShape", at = @At("HEAD"), cancellable = true)
    private void useCollisionVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext, CallbackInfoReturnable<VoxelShape> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_15_2)) {
            cir.setReturnValue(this.getCollisionShape(blockState, blockGetter, blockPos, collisionContext));
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initShapes1_8(Properties settings, CallbackInfo ci) {
        final float f = 7.0F;
        final float g = 9.0F;
        final float h = 7.0F;
        final float i = 9.0F;

        final VoxelShape baseShape = Block.box(f, 0.0, f, g, (float) 16.0, g);

        viaFabricPlus$shape_r1_12_2 = new VoxelShape[]{
            baseShape,
            Block.box(h, 0.0, h, i, 16.0, 16.0),
            Block.box(0.0, 0.0, h, i, 16.0, i),
            Block.box(0.0, 0.0, h, i, 16.0, 16.0),
            Block.box(h, 0.0, 0.0, i, 16.0, i),
            Block.box(h, 0.0, 0.0, i, 16.0, 16.0),
            Block.box(0.0, 0.0, 0.0, i, 16.0, i),
            Block.box(0.0, 0.0, 0.0, i, 16.0, 16.0),
            Block.box(h, 0.0, h, 16.0, 16.0, i),
            Block.box(h, 0.0, h, 16.0, 16.0, 16.0),
            Block.box(0.0, 0.0, h, 16.0, 16.0, i),
            Block.box(0.0, 0.0, h, 16.0, 16.0, 16.0),
            Block.box(h, 0.0, 0.0, 16.0, 16.0, i),
            Block.box(h, 0.0, 0.0, 16.0, 16.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 16.0, i),
            Shapes.block()
        };

        final VoxelShape northShape = Block.box(h, (float) 0.0, 0.0, i, (float) 16.0, i - 1);
        final VoxelShape southShape = Block.box(h, (float) 0.0, h + 1, i, (float) 16.0, 16.0);
        final VoxelShape westShape = Block.box(0.0, (float) 0.0, h, i - 1, (float) 16.0, i);
        final VoxelShape eastShape = Block.box(h + 1, (float) 0.0, h, 16.0, (float) 16.0, i);

        final VoxelShape northEastCornerShape = Shapes.or(northShape, eastShape);
        final VoxelShape southWestCornerShape = Shapes.or(southShape, westShape);

        viaFabricPlus$shape_r1_8 = new VoxelShape[]{
            baseShape,
            southShape,
            westShape,
            southWestCornerShape,
            northShape,
            Shapes.or(southShape, northShape),
            Shapes.or(westShape, northShape),
            Shapes.or(southWestCornerShape, northShape),
            eastShape,
            Shapes.or(southShape, eastShape),
            Shapes.or(westShape, eastShape),
            Shapes.or(southWestCornerShape, eastShape),
            northEastCornerShape,
            Shapes.or(southShape, northEastCornerShape),
            Shapes.or(westShape, northEastCornerShape),
            Shapes.or(southWestCornerShape, northEastCornerShape)
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (DebugSettings.INSTANCE.legacyPaneOutlines.isEnabled()) {
            return this.viaFabricPlus$shape_r1_12_2[this.viaFabricPlus$getShapeIndex(state)];
        } else {
            return super.getShape(state, world, pos, context);
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            return this.viaFabricPlus$shape_r1_8[this.viaFabricPlus$getShapeIndex(state)];
        } else {
            return super.getCollisionShape(state, world, pos, context);
        }
    }

}
