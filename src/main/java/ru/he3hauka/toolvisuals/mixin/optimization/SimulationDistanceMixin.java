package ru.he3hauka.toolvisuals.mixin.optimization;

import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import ru.he3hauka.toolvisuals.client.optimization.preset.GameOptionLimits;

@Mixin(Options.class)
public abstract class SimulationDistanceMixin {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance$IntRange;<init>(IIZ)V"),
            index = 0, slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=options.simulationDistance"),
            to = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;simulationDistance:Lnet/minecraft/client/OptionInstance;")), require = 1)
    private int toolvisuals$simulationMinimum(int original) {
        return GameOptionLimits.simulationMinimum(original);
    }
}
