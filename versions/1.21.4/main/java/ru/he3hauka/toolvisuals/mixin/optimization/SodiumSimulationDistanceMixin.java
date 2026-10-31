package ru.he3hauka.toolvisuals.mixin.optimization;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.he3hauka.toolvisuals.client.optimization.preset.GameOptionLimits;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.SodiumGameOptionPages", remap = false)
public abstract class SodiumSimulationDistanceMixin {
    @ModifyArg(method = "lambda$general$3", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/gui/options/control/SliderControl;<init>(Lnet/caffeinemc/mods/sodium/client/gui/options/Option;IIILnet/caffeinemc/mods/sodium/client/gui/options/control/ControlValueFormatter;)V"),
            index = 1, require = 1, remap = false)
    private static int toolvisuals$simulationMinimum(int original) {
        return GameOptionLimits.simulationMinimum(original);
    }
}
