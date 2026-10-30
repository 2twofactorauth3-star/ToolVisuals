package com.viaversion.viafabricplus.injection.mixin.features.execute_inputs_sync;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

import com.viaversion.viafabricplus.injection.access.execute_inputs_sync.IMouseKeyboardHandlers;
import com.viaversion.viafabricplus.settings.impl.DebugSettings;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public abstract class MixinMouseHandler implements IMouseKeyboardHandlers {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private final Queue<Runnable> viaFabricPlus$pendingScreenEvents = new ConcurrentLinkedQueue<>();

    @Redirect(method = {"lambda$setup$5", "lambda$setup$7"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;execute(Ljava/lang/Runnable;)V"))
    private void storeEvent(Minecraft instance, Runnable runnable) {
        if (this.minecraft.getConnection() != null && this.minecraft.screen != null && DebugSettings.INSTANCE.executeInputsSynchronously.isEnabled()) {
            this.viaFabricPlus$pendingScreenEvents.offer(runnable);
        } else {
            instance.execute(runnable);
        }
    }

    @Override
    public Queue<Runnable> viaFabricPlus$getPendingScreenEvents() {
        return this.viaFabricPlus$pendingScreenEvents;
    }

}
