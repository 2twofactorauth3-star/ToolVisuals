package com.viaversion.viafabricplus.screen.impl.realms;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import com.viaversion.viafabricplus.screen.VFPScreen;
import java.util.function.Consumer;

public final class AcceptInvitationCodeScreen extends VFPScreen {

    private final Consumer<String> serviceHandler;

    public AcceptInvitationCodeScreen(Consumer<String> serviceHandler) {
        super(Component.translatable("screen.viafabricplus.accept_invite"), true);

        this.serviceHandler = serviceHandler;
    }

    @Override
    protected void init() {
        super.init();
        setupDefaultSubtitle();

        final EditBox codeField = new EditBox(font, this.width / 2 - 100, this.height / 2 - 10, 200, 20, Component.empty());
        codeField.setHint(Component.translatable("base.viafabricplus.code"));

        this.addRenderableWidget(codeField);

        this.addRenderableWidget(Button.builder(Component.translatable("base.viafabricplus.accept"), button -> {
            this.serviceHandler.accept(codeField.getValue());
            onClose();
        }).pos(this.width / 2 - Button.DEFAULT_WIDTH / 2, this.height / 2 + 20).build());
    }

}
