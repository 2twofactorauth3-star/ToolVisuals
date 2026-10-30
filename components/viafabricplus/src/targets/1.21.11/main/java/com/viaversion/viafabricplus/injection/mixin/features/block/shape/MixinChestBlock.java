package com.viaversion.viafabricplus.injection.mixin.features.block.shape;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import java.util.Map;
import java.util.function.Supplier;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlock.class)
public abstract class MixinChestBlock extends AbstractChestBlock<ChestBlockEntity> {
   @Unique
   private static final VoxelShape viaFabricPlus$single_chest_shape_bedrock = Shapes.box(0.025, 0.0, 0.025, 0.975, 0.95, 0.975);
   @Unique
   private static final Map<Direction, VoxelShape> viaFabricPlus$double_chest_shapes_bedrock = Map.of(
      Direction.NORTH,
      Shapes.box(0.025, 0.0, 0.0, 0.975, 0.95, 0.975),
      Direction.SOUTH,
      Shapes.box(0.025, 0.0, 0.025, 0.975, 0.95, 1.0),
      Direction.WEST,
      Shapes.box(0.0, 0.0, 0.025, 0.975, 0.95, 0.975),
      Direction.EAST,
      Shapes.box(0.025, 0.0, 0.025, 1.0, 0.95, 0.975)
   );
   @Shadow
   @Final
   private static Map<Direction, VoxelShape> HALF_SHAPES;
   @Shadow
   @Final
   private static VoxelShape SHAPE;
   @Shadow
   @Final
   public static EnumProperty<ChestType> TYPE;

   @Shadow
   public static Direction getConnectedDirection(BlockState state) {
      return null;
   }

   protected MixinChestBlock(Properties settings, Supplier<BlockEntityType<? extends ChestBlockEntity>> blockEntityTypeSupplier) {
      super(settings, blockEntityTypeSupplier);
   }

   @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
   private void changeOutlineShape(BlockState var1, BlockGetter var2, BlockPos var3, CollisionContext var4, CallbackInfoReturnable<VoxelShape> var5) {
      if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.r1_4_2)) {
         var5.setReturnValue(Shapes.block());
      } else {
         ProtocolTranslator.getTargetVersion();
         Object var10001 = null;
         boolean var10000 = false;
      }
   }

   public VoxelShape getOcclusionShape(BlockState var1) {
      if (!ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.r1_4_2)) {
         ProtocolTranslator.getTargetVersion();
         Object var10001 = null;
         boolean var10000 = false;
         return super.getOcclusionShape(var1);
      } else {
         return var1.getValue(ChestBlock.TYPE) == ChestType.SINGLE ? SHAPE : HALF_SHAPES.get(ChestBlock.getConnectedDirection(var1));
      }
   }
}
