package ru.he3hauka.toolvisuals.mixin.optimization;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import ru.he3hauka.toolvisuals.client.optimization.preset.GameOptionLimits;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.SodiumConfigBuilder", remap = false)
public abstract class SodiumSimulationDistanceMixin {
    @ModifyArg(method = "buildGeneralPage", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/api/config/structure/IntegerOptionBuilder;setRange(III)Lnet/caffeinemc/mods/sodium/api/config/structure/IntegerOptionBuilder;", ordinal = 0),
            index = 0, slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=options.simulationDistance")), require = 1, remap = false)
    private int toolvisuals$simulationMinimum(int original) {
        return GameOptionLimits.simulationMinimum(original);
    }
}
