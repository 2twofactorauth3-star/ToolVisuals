package ru.he3hauka.toolvisuals.mixin.tapemouse;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.he3hauka.toolvisuals.client.input.tape.TapeMouse;

@Mixin(Minecraft.class)
public abstract class TapeMouseGameplayMixin {
    @WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z"))
    private boolean toolvisuals$tapeClick(KeyMapping mapping, Operation<Boolean> original) {
        return TapeMouse.consumeClick((Minecraft)(Object)this, mapping, original.call(mapping));
    }

    @ModifyExpressionValue(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;isMouseGrabbed()Z"))
    private boolean toolvisuals$tapeMining(boolean original) {
        return original || TapeMouse.holdingAttack((Minecraft)(Object)this);
    }
}
