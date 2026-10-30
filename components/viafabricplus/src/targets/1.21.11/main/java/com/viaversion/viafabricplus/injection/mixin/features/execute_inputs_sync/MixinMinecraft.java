package com.viaversion.viafabricplus.injection.mixin.features.execute_inputs_sync;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

import com.viaversion.viafabricplus.injection.access.execute_inputs_sync.IMouseKeyboardHandlers;
import com.viaversion.viafabricplus.settings.impl.DebugSettings;
import java.util.Queue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow
    @Final
    public MouseHandler mouseHandler;

    @Shadow
    @Final
    public KeyboardHandler keyboardHandler;

    @Inject(method = "tick",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", ordinal = 0),
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;missTime:I", ordinal = 0),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;fillCrashDetails(Lnet/minecraft/CrashReport;)V")
        )
    )
    private void processInputQueues(CallbackInfo ci) {
        if (DebugSettings.INSTANCE.executeInputsSynchronously.isEnabled()) {
            Queue<Runnable> inputEvents = ((IMouseKeyboardHandlers) this.mouseHandler).viaFabricPlus$getPendingScreenEvents();
            while (!inputEvents.isEmpty()) inputEvents.poll().run();

            inputEvents = ((IMouseKeyboardHandlers) this.keyboardHandler).viaFabricPlus$getPendingScreenEvents();
            while (!inputEvents.isEmpty()) inputEvents.poll().run();
        }
    }

}
