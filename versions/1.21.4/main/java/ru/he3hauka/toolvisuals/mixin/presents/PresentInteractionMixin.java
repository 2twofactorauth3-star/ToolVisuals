package ru.he3hauka.toolvisuals.mixin.presents;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.presents.ToolPresents;

@Mixin(MultiPlayerGameMode.class)
public abstract class PresentInteractionMixin {
    @Inject(method = "useItemOn", at = @At("RETURN"))
    private void toolvisuals$presentClicked(LocalPlayer player, InteractionHand hand, BlockHitResult hit,
                                            CallbackInfoReturnable<InteractionResult> result) {
        ToolPresents.clicked(hit.getBlockPos());
    }
    @Inject(method = "interactAt", at = @At("RETURN"))
    private void toolvisuals$presentEntityClicked(Player player, Entity entity, EntityHitResult hit, InteractionHand hand,
                                                 CallbackInfoReturnable<InteractionResult> result) {
        if (entity instanceof ArmorStand || entity instanceof Interaction || entity instanceof Display) {
            ToolPresents.clicked(entity.blockPosition());
            ToolPresents.clicked(BlockPos.containing(hit.getLocation()));
        }
    }
}
