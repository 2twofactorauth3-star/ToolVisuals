package ru.he3hauka.toolvisuals.mixin.optimization;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.MinecraftDiagnostics;

@Mixin(Minecraft.class)
public abstract class FrameDiagnosticsMixin {
    @WrapMethod(method="runTick")
    private void toolvisuals$fullFrame(boolean render,Operation<Void> original) {
        if(!Diagnostics.enabled()){original.call(render);return;}
        var client=(Minecraft)(Object)this;
        long token=Diagnostics.openFrame(MinecraftDiagnostics.context(client,render));
        try{original.call(render);}finally{Diagnostics.endFrame(token,MinecraftDiagnostics.context(client,render));}
    }
}
