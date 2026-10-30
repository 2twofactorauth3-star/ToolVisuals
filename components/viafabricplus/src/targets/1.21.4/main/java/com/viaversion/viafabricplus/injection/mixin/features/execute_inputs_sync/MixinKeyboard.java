package com.viaversion.viafabricplus.injection.mixin.features.execute_inputs_sync;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;

import com.viaversion.viafabricplus.injection.access.execute_inputs_sync.IMouseKeyboard;
import com.viaversion.viafabricplus.settings.impl.DebugSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mixin(Keyboard.class)
public abstract class MixinKeyboard implements IMouseKeyboard {

    @Shadow
    @Final
    private MinecraftClient client;

    @Unique
    private final Queue<Runnable> viaFabricPlus$pendingScreenEvents = new ConcurrentLinkedQueue<>();

    @Redirect(method = {"method_22676", "method_22675"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;execute(Ljava/lang/Runnable;)V"))
    private void storeEvent(MinecraftClient instance, Runnable runnable) {
        if (this.client.getNetworkHandler() != null && this.client.currentScreen != null && DebugSettings.INSTANCE.executeInputsSynchronously.isEnabled()) {
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
