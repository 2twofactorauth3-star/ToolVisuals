package com.viaversion.viafabricplus.injection.mixin.features.interaction.container_clicking;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreen extends Screen {

    @Shadow
    public abstract boolean mouseClicked(final @NonNull MouseButtonEvent mouseButtonEvent, final boolean bl);

    protected MixinAbstractContainerScreen(final Component component) {
        super(component);
    }

    @Redirect(method = {"mouseClicked", "mouseReleased"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/MouseButtonEvent;hasShiftDown()Z"))
    private boolean disableShiftClickItems(final MouseButtonEvent instance) {
        return instance.hasShiftDown() && ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(LegacyProtocolVersion.r1_6_1);
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void disableItemDragging(final MouseButtonEvent mouseButtonEvent, final double mouseX, final double mouseY, final CallbackInfoReturnable<Boolean> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThan(LegacyProtocolVersion.r1_5tor1_5_1)) {
            cir.setReturnValue(super.mouseDragged(mouseButtonEvent, mouseX, mouseY));
        }
    }

    @WrapWithCondition(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V", ordinal = 0))
    private boolean disableItemCloning(final AbstractContainerScreen<?> instance, final Slot slot, final int slotIndex, final int count, final ClickType clickType) {
        return ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(LegacyProtocolVersion.r1_4_2);
    }

    @Redirect(method = "checkHotbarKeyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;matches(Lnet/minecraft/client/input/KeyEvent;)Z", ordinal = 1))
    private boolean disableHotbarKeys(final KeyMapping instance, final KeyEvent keyEvent) {
        return instance.matches(keyEvent) && ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(LegacyProtocolVersion.r1_4_2);
    }

}
