package ru.he3hauka.toolvisuals.mixin.items;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.MINI_ITEMS;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

@Mixin(ItemEntityRenderer.class)
public abstract class MiniItemsMixin {
    @WrapMethod(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V")
    private void toolvisuals$miniItems(ItemEntityRenderState state, PoseStack pose,
            SubmitNodeCollector nodes, CameraRenderState camera, Operation<Void> original) {
        var config = ToolVisualsClient.CONFIG;
        if (!config.active(MINI_ITEMS) || !config.flag(MINI_DROPPED)) {
            original.call(state, pose, nodes, camera); return;
        }
        pose.pushPose();
        try {
            float scale = config.number(MINI_DROPPED_SCALE);
            pose.scale(scale, scale, scale);
            original.call(state, pose, nodes, camera);
        } finally { pose.popPose(); }
    }
}
