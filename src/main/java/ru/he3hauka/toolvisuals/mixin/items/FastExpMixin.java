package ru.he3hauka.toolvisuals.mixin.items;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.FAST_EXP;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.FAST_EXP_DELAY;

@Mixin(Minecraft.class)
public abstract class FastExpMixin {
    @Shadow private int rightClickDelay;
    @WrapOperation(method="startUseItem",at=@At(value="INVOKE",target="Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult toolvisuals$fastBottles(MultiPlayerGameMode gameMode,Player player,InteractionHand hand,Operation<InteractionResult> original) {
        boolean bottle = player.getItemInHand(hand).is(Items.EXPERIENCE_BOTTLE);
        var result = original.call(gameMode,player,hand);
        if (bottle && result.consumesAction() && ToolVisualsClient.CONFIG.active(FAST_EXP)){
            rightClickDelay = Math.min(rightClickDelay,(int)ToolVisualsClient.CONFIG.value(FAST_EXP_DELAY));
            if(ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.action(FAST_EXP,"delay_applied","ticks="+rightClickDelay);
        }
        return result;
    }
}
