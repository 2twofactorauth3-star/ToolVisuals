package ru.he3hauka.toolvisuals.mixin.itemhighlights;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import ru.he3hauka.toolvisuals.client.items.highlight.render.HandItemGlow;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemGlowHandContextMixin {
    @WrapMethod(method = "renderArmWithItem")
    private void toolvisuals$heldItem(AbstractClientPlayer player, float partial, float pitch, InteractionHand hand, float swing, ItemStack item, float equip, PoseStack pose, SubmitNodeCollector nodes, int light, Operation<Void> original) {
        int previous = HandItemGlow.hand(item);
        try { original.call(player, partial, pitch, hand, swing, item, equip, pose, nodes, light); }
        finally { HandItemGlow.restoreHand(previous); }
    }
}
