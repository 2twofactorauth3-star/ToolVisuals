package com.viaversion.viafabricplus.injection.mixin.features.mouse_sensitivity;

import net.minecraft.client.Mouse;
import net.minecraft.client.option.SimpleOption;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viaversion.viafabricplus.features.mouse_sensitivity.MouseSensitivity1_13_2;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mouse.class)
public abstract class MixinMouse {

    @WrapOperation(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;", ordinal = 0))
    private Object adjustMouseSensitivity1_13_2(SimpleOption<Double> instance, Operation<Double> original) {
        final Double value = original.call(instance);

        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
            return (double) MouseSensitivity1_13_2.get1_13SliderValue(value.floatValue()).keyFloat();
        } else {
            return value;
        }
    }

}
