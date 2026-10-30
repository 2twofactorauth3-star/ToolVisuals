package ru.he3hauka.toolvisuals.mixin.holyworld;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.connection.policy.FeatureKeys;

@Mixin(KeyBindsList.class)
public abstract class FeatureKeysMixin {
    @ModifyVariable(method = "<init>", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private KeyMapping[] toolvisuals$availableKeys(KeyMapping[] mappings) {
        return FeatureKeys.filter(ToolVisualsClient.CONFIG, mappings);
    }
}
