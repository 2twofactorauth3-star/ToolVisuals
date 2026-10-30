package com.viaversion.viafabricplus.injection.mixin.features.execute_inputs_sync;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;

import com.viaversion.viafabricplus.injection.access.execute_inputs_sync.IMouseKeyboard;
import com.viaversion.viafabricplus.settings.impl.DebugSettings;
import java.util.Queue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Shadow
    @Final
    public Mouse mouse;

    @Shadow
    @Final
    public Keyboard keyboard;

    @Inject(method = "tick",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", ordinal = 0),
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;attackCooldown:I", ordinal = 0),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;addCrashReportSection(Lnet/minecraft/util/crash/CrashReport;)V")
            )
    )
    private void processInputQueues(CallbackInfo ci) {
        if (DebugSettings.INSTANCE.executeInputsSynchronously.isEnabled()) {
            Queue<Runnable> inputEvents = ((IMouseKeyboard) this.mouse).viaFabricPlus$getPendingScreenEvents();
            while (!inputEvents.isEmpty()) inputEvents.poll().run();

            inputEvents = ((IMouseKeyboard) this.keyboard).viaFabricPlus$getPendingScreenEvents();
            while (!inputEvents.isEmpty()) inputEvents.poll().run();
        }
    }

}
