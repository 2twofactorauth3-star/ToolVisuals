package com.viaversion.viafabricplus.injection.mixin.features.interaction.container_clicking;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import com.viaversion.viafabricplus.injection.access.interaction.container_clicking.IAbstractContainerMenu;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractContainerMenu.class)
public abstract class MixinAbstractContainerMenu implements IAbstractContainerMenu {

    @Shadow
    private ItemStack carried;

    @Unique
    private short viaFabricPlus$actionId = 0;

    @Redirect(method = "initializeContents", at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;carried:Lnet/minecraft/world/item/ItemStack;"))
    private void preventUpdate(AbstractContainerMenu instance, ItemStack value) {
        if (ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_17_1)) {
            this.carried = value;
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
