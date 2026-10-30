package com.viaversion.viafabricplus.injection.mixin.features.block.shape;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.Map;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LadderBlock.class)
public abstract class MixinLadderBlock extends Block {
   @Unique
   private static final Map<Direction, VoxelShape> viaFabricPlus$shapes_r1_8_x = Map.of(
      Direction.NORTH,
      Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0),
      Direction.SOUTH,
      Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0),
      Direction.WEST,
      Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0),
      Direction.EAST,
      Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0)
   );
   @Unique
   private static final Map<Direction, VoxelShape> viaFabricPlus$shapes_bedrock = Map.of(
      Direction.NORTH,
      Shapes.box(0.0, 0.0, 0.8125, 1.0, 1.0, 1.0),
      Direction.SOUTH,
      Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0, 0.1875),
      Direction.WEST,
      Shapes.box(0.8125, 0.0, 0.0, 1.0, 1.0, 1.0),
      Direction.EAST,
      Shapes.box(0.0, 0.0, 0.0, 0.1875, 1.0, 1.0)
   );
   @Shadow
   @Final
   public static Map<Direction, VoxelShape> SHAPES;
   @Shadow
   @Final
   public static EnumProperty<Direction> FACING;

   public MixinLadderBlock(Properties settings) {
      super(settings);
   }

   @Redirect(method = "getShape", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/LadderBlock;SHAPES:Ljava/util/Map;"))
   private Map<Direction, VoxelShape> changeOutlineShape() {
      if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
         return viaFabricPlus$shapes_r1_8_x;
      } else {
         ProtocolTranslator.getTargetVersion();
         Object var10001 = null;
         boolean var10000 = false;
         return SHAPES;
      }
   }

   public VoxelShape getOcclusionShape(BlockState var1) {
      if (!ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
         ProtocolTranslator.getTargetVersion();
         Object var10001 = null;
         boolean var10000 = false;
         return super.getOcclusionShape(var1);
      } else {
         return SHAPES.get(var1.getValue(FACING));
      }
   }
}
