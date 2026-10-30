package ru.he3hauka.toolvisuals.mixin.replay;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.he3hauka.toolvisuals.client.replay.ui.ReplayViewerScreen;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

@Mixin(Minecraft.class)
public abstract class ReplayFrameMixin {
    @WrapOperation(method="runTick",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V"))
    private void toolvisuals$render(GameRenderer renderer,DeltaTracker delta,boolean tick,Operation<Void> original) {
        var client=(Minecraft)(Object)this;
        if(!(client.screen instanceof ReplayViewerScreen viewer)){original.call(renderer,delta,tick);return;}
        if(client.getOverlay()!=null){viewer.onClose();original.call(renderer,delta,tick);return;}
        try{viewer.frame();}catch(Exception error){viewer.failure(error);original.call(renderer,delta,tick);return;}
        viewer.renderWorld(()->original.call(client.gameRenderer,ReplayScope.active()?ReplayScope.scene().delta():delta,tick));
    }
}
