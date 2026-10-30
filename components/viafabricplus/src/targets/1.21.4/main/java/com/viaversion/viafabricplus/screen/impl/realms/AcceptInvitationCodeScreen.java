package com.viaversion.viafabricplus.screen.impl.realms;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import com.viaversion.viafabricplus.screen.VFPScreen;
import java.util.function.Consumer;

public final class AcceptInvitationCodeScreen extends VFPScreen {

    private final Consumer<String> serviceHandler;

    public AcceptInvitationCodeScreen(Consumer<String> serviceHandler) {
        super(Text.translatable("screen.viafabricplus.accept_invite"), true);

        this.serviceHandler = serviceHandler;
    }

    @Override
    protected void init() {
        super.init();
        setupDefaultSubtitle();

        final TextFieldWidget codeField = new TextFieldWidget(textRenderer, this.width / 2 - 100, this.height / 2 - 10, 200, 20, Text.empty());
        codeField.setPlaceholder(Text.translatable("base.viafabricplus.code"));

        this.addDrawableChild(codeField);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("base.viafabricplus.accept"), button -> {
            this.serviceHandler.accept(codeField.getText());
            close();
        }).position(this.width / 2 - ButtonWidget.DEFAULT_WIDTH / 2, this.height / 2 + 20).build());
    }

}
