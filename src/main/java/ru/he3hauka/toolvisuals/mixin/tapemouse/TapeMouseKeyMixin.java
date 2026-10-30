package ru.he3hauka.toolvisuals.mixin.tapemouse;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.he3hauka.toolvisuals.client.input.tape.TapeMouse;

@Mixin(KeyMapping.class)
public abstract class TapeMouseKeyMixin {
    @ModifyReturnValue(method = "isDown", at = @At("RETURN"))
    private boolean toolvisuals$tapeDown(boolean original) {
        return original || TapeMouse.down(Minecraft.getInstance(), (KeyMapping)(Object)this, false);
    }
}
