package com.viaversion.viafabricplus.injection.mixin.features.interaction.replace_block_item_use_logic;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viafabricplus.protocoltranslator.impl.ViaFabricPlusMappingDataLoader;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemPlacementContext.class)
public abstract class MixinItemPlacementContext extends ItemUsageContext {

    public MixinItemPlacementContext(PlayerEntity player, Hand hand, BlockHitResult hit) {
        super(player, hand, hit);
    }

    @Inject(method = "getPlayerLookDirection", at = @At("HEAD"), cancellable = true)
    private void getPlayerLookDirection1_12_2(CallbackInfoReturnable<Direction> cir) {
        final ItemPlacementContext self = (ItemPlacementContext) (Object) this;
        final PlayerEntity player = self.getPlayer();

        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            final BlockPos placementPos = self.getBlockPos();
            final double blockPosCenterFactor = ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_10) ? 0.5 : 0;

            if (Math.abs(player.getX() - (placementPos.getX() + blockPosCenterFactor)) < 2 && Math.abs(player.getZ() - (placementPos.getZ() + blockPosCenterFactor)) < 2) {
                final double eyeY = player.getY() + player.getEyeHeight(player.getPose());

                if (eyeY - placementPos.getY() > 2) {
                    cir.setReturnValue(Direction.DOWN);
                    return;
                }

                if (placementPos.getY() - eyeY > 0) {
                    cir.setReturnValue(Direction.UP);
                    return;
                }
            }

            cir.setReturnValue(player.getHorizontalFacing());
        }
    }

    @Inject(method = "canPlace", at = @At("RETURN"), cancellable = true)
    private void canPlace1_12_2(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            cir.setReturnValue(ViaFabricPlusMappingDataLoader.getBlockMaterial(this.getWorld().getBlockState(this.getBlockPos()).getBlock()).equals("decoration") && Block.getBlockFromItem(this.getStack().getItem()).equals(Blocks.ANVIL));
        }
    }

}
