package com.viaversion.viafabricplus.injection.mixin.features.mouse_sensitivity;

import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.MouseSettingsScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import com.viaversion.viafabricplus.features.mouse_sensitivity.MouseSensitivity1_13_2;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MouseSettingsScreen.class)
public abstract class MixinMouseSettingsScreen extends OptionsSubScreen {

    public MixinMouseSettingsScreen(Screen parent, Options gameOptions, Component title) {
        super(parent, gameOptions, title);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2) && this.list.findOption(this.options.sensitivity()).isHovered()) {
            context.setTooltipForNextFrame(font, Component.nullToEmpty("<=1.13.2 Sensitivity: " + MouseSensitivity1_13_2.get1_13SliderValue(this.options.sensitivity().get().floatValue()).valueInt() + "%"), mouseX, mouseY);
        }
    }

}
