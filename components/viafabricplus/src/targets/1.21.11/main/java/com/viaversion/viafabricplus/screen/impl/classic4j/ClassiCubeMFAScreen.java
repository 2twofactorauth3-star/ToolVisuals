package com.viaversion.viafabricplus.screen.impl.classic4j;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import com.viaversion.viafabricplus.save.SaveManager;
import com.viaversion.viafabricplus.screen.VFPScreen;
import de.florianreuth.classic4j.ClassiCubeHandler;
import de.florianreuth.classic4j.api.LoginProcessHandler;
import de.florianreuth.classic4j.model.classicube.account.CCAccount;

public final class ClassiCubeMFAScreen extends VFPScreen {

    public static final ClassiCubeMFAScreen INSTANCE = new ClassiCubeMFAScreen();

    public ClassiCubeMFAScreen() {
        super(Component.translatable("screen.viafabricplus.classicube_mfa"), true);
    }

    private EditBox mfaField;

    @Override
    protected void init() {
        super.init();
        if (this.getSubtitle() == null) {
            this.setupSubtitle(Component.translatable("classic4j_library.viafabricplus.error.logincode"));
        }

        this.addRenderableWidget(mfaField = new EditBox(font, width / 2 - 150, 70 + 10, 300, 20, Component.empty()));

        mfaField.setHint(Component.nullToEmpty("MFA"));

        this.addRenderableWidget(Button.builder(Component.translatable("base.viafabricplus.login"), button -> {
            this.setupSubtitle(Component.translatable("classicube.viafabricplus.loading"));
            final CCAccount account = SaveManager.INSTANCE.getAccountsSave().getClassicubeAccount();

            ClassiCubeHandler.requestAuthentication(account, mfaField.getValue(), new LoginProcessHandler() {
                @Override
                public void handleMfa(CCAccount account) {

                }

                @Override
                public void handleSuccessfulLogin(CCAccount account) {
                    ClassiCubeServerListScreen.INSTANCE.open(prevScreen);
                }

                @Override
                public void handleException(Throwable throwable) {
                    setupSubtitle(Component.nullToEmpty(throwable.getMessage()));
                }
            });
        }).pos(width / 2 - 75, mfaField.getY() + (20 * 4) + 5).size(150, 20).build());
    }

    @Override
    public void onClose() {

        SaveManager.INSTANCE.getAccountsSave().setClassicubeAccount(null);
        super.onClose();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.renderScreenTitle(context);
    }

}
