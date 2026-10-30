package com.viaversion.viafabricplus.injection.mixin.features.v1_12_2;

import com.mojang.blaze3d.platform.SDLEventHandler;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viafabricplus.injection.access.v1_12_2.ISDLEventHandler;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.Queue;
import net.minecraft.client.Minecraft;
import org.objectweb.asm.Opcodes;
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
    private SDLEventHandler sdlEventHandler;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;screen()Lnet/minecraft/client/gui/screens/Screen;", ordinal = 0), slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;missTime:I", ordinal = 0, opcode = Opcodes.PUTFIELD), to = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;fillReportDetails(Lnet/minecraft/CrashReport;)Lnet/minecraft/CrashReportCategory;")))
    private void processInputQueues(CallbackInfo ci) {
        if (ViaFabricPlus.api().targetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            final Queue<Runnable> inputEvents = ((ISDLEventHandler) this.sdlEventHandler).viaFabricPlus$getPendingScreenEvents();
            while (!inputEvents.isEmpty()) inputEvents.poll().run();
        }
    }

}
