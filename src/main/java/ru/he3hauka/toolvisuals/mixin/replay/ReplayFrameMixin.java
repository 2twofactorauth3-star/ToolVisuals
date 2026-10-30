package ru.he3hauka.toolvisuals.mixin.replay;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.he3hauka.toolvisuals.client.replay.ui.ReplayViewerScreen;

@Mixin(Minecraft.class)
public abstract class ReplayFrameMixin {
    @WrapMethod(method = "renderFrame")
    private void toolvisuals$replayFrame(boolean tick, Operation<Void> original) {
        var client = (Minecraft)(Object)this;
        if (!(client.gui.screen() instanceof ReplayViewerScreen viewer)) { original.call(tick); return; }
        if(client.gui.overlay()!=null){viewer.onClose();original.call(tick);return;}
        try { viewer.frame(); }
        catch (Exception error) { viewer.failure(error); original.call(tick); return; }
        viewer.renderWorld(() -> original.call(tick));
    }
    @ModifyArg(method="renderFrame",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/GameRenderer;update(Lnet/minecraft/client/DeltaTracker;)V"),index=0)
    private net.minecraft.client.DeltaTracker toolvisuals$replayUpdateDelta(net.minecraft.client.DeltaTracker original){return toolvisuals$replayDelta(original);}
    @ModifyArg(method="renderFrame",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/GameRenderer;extract(Lnet/minecraft/client/DeltaTracker;Z)V"),index=0)
    private net.minecraft.client.DeltaTracker toolvisuals$replayExtractDelta(net.minecraft.client.DeltaTracker original){return toolvisuals$replayDelta(original);}
    @Unique private static net.minecraft.client.DeltaTracker toolvisuals$replayDelta(net.minecraft.client.DeltaTracker original){
        return ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active()?ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.scene().delta():original;
    }
}
