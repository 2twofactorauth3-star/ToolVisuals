package com.viaversion.viafabricplus.injection.mixin.features.remove_newer_screen_features;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.properties.StructureMode;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureBlockEditScreen.class)
public abstract class MixinStructureBlockEditScreen {

    @Shadow
    private CycleButton<Boolean> strictButton;

    @Shadow
    @Final
    private static Component STRICT_LABEL;

    @Shadow
    private EditBox nameEdit;

    @Inject(method = "updateMode", at = @At("RETURN"))
    private void hideStrictButton(StructureMode mode, CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21_4)) {
            strictButton.visible = false;
        }
    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"))
    private boolean hideStrictText(GuiGraphics instance, Font textRenderer, Component text, int x, int y, int color) {
        if (text == STRICT_LABEL) {
            return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_21_4);
        } else {
            return true;
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void changeInputNameMaxLength(CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_18_2)) {
            this.nameEdit.setMaxLength(64);
        }
    }

}
