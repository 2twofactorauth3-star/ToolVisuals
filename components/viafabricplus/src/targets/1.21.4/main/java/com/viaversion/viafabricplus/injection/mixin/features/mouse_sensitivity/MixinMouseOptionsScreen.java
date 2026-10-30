package com.viaversion.viafabricplus.injection.mixin.features.mouse_sensitivity;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.MouseOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;

import com.viaversion.viafabricplus.features.mouse_sensitivity.MouseSensitivity1_13_2;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MouseOptionsScreen.class)
public abstract class MixinMouseOptionsScreen extends GameOptionsScreen {

    public MixinMouseOptionsScreen(Screen parent, GameOptions gameOptions, Text title) {
        super(parent, gameOptions, title);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2) && this.body.getWidgetFor(this.gameOptions.getMouseSensitivity()).isHovered()) {
            context.drawTooltip(textRenderer, Text.of("<=1.13.2 Sensitivity: " + MouseSensitivity1_13_2.get1_13SliderValue(this.gameOptions.getMouseSensitivity().getValue().floatValue()).valueInt() + "%"), mouseX, mouseY);
        }
    }

}
