package ru.he3hauka.toolvisuals.mixin.optimization;

import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.optimization.preset.StartupPreset;

@Mixin(Options.class)
public abstract class StartupGraphicsMixin {
    @Inject(method="setGraphicsPresetToCustom",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$startupPreset(CallbackInfo ci){if(StartupPreset.applying())ci.cancel();}
}
