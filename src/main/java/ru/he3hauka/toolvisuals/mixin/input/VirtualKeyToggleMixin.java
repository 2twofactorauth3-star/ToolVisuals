package ru.he3hauka.toolvisuals.mixin.input;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.ToggleKeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import ru.he3hauka.toolvisuals.client.input.VirtualKeyScope;

@Mixin(ToggleKeyMapping.class)
public abstract class VirtualKeyToggleMixin {
    @WrapMethod(method = "setDown")
    private void toolvisuals$physicalToggle(boolean down, Operation<Void> original) {
        try (var ignored = VirtualKeyScope.physicalUpdate()) { original.call(down); }
    }
}
