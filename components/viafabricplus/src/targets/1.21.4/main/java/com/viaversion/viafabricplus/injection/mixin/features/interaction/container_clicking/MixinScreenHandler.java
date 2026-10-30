package com.viaversion.viafabricplus.injection.mixin.features.interaction.container_clicking;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

import com.viaversion.viafabricplus.injection.access.interaction.container_clicking.IScreenHandler;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ScreenHandler.class)
public abstract class MixinScreenHandler implements IScreenHandler {

    @Shadow
    private ItemStack cursorStack;

    @Unique
    private short viaFabricPlus$actionId = 0;

    @Redirect(method = "updateSlotStacks", at = @At(value = "FIELD", target = "Lnet/minecraft/screen/ScreenHandler;cursorStack:Lnet/minecraft/item/ItemStack;"))
    private void preventUpdate(ScreenHandler instance, ItemStack value) {
        if (ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_17_1)) {
            this.cursorStack = value;
        }
    }

    @Override
    public short viaFabricPlus$getActionId() {
        return viaFabricPlus$actionId;
    }

    @Override
    public short viaFabricPlus$incrementAndGetActionId() {
        return ++viaFabricPlus$actionId;
    }

}
