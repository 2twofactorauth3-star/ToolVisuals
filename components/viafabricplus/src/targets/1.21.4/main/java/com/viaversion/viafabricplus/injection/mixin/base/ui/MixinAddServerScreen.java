package com.viaversion.viafabricplus.injection.mixin.base.ui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import com.viaversion.viafabricplus.injection.access.base.IServerInfo;
import com.viaversion.viafabricplus.screen.impl.PerServerVersionScreen;
import com.viaversion.viafabricplus.settings.impl.GeneralSettings;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AddServerScreen.class)
public abstract class MixinAddServerScreen extends Screen {

    @Shadow
    @Final
    private ServerInfo server;

    @Shadow
    private TextFieldWidget serverNameField;

    @Shadow
    private TextFieldWidget addressField;

    @Unique
    private String viaFabricPlus$nameField;

    @Unique
    private String viaFabricPlus$addressField;

    public MixinAddServerScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addVersionSetterButton(CallbackInfo ci) {
        final int buttonPosition = GeneralSettings.INSTANCE.addServerScreenButtonOrientation.getIndex();
        if (buttonPosition == 0) {
            return;
        }

        final IServerInfo mixinServerInfo = (IServerInfo) server;
        final ProtocolVersion forcedVersion = mixinServerInfo.viaFabricPlus$forcedVersion();

        if (viaFabricPlus$nameField != null && viaFabricPlus$addressField != null) {
            this.serverNameField.setText(viaFabricPlus$nameField);
            this.addressField.setText(viaFabricPlus$addressField);

            viaFabricPlus$nameField = null;
            viaFabricPlus$addressField = null;
        }

        ButtonWidget.Builder buttonBuilder = ButtonWidget.builder(forcedVersion == null ? Text.translatable("base.viafabricplus.set_version") : Text.of(forcedVersion.getName()), button -> {

            viaFabricPlus$nameField = serverNameField.getText();
            viaFabricPlus$addressField = addressField.getText();

            client.setScreen(new PerServerVersionScreen(this, mixinServerInfo::viaFabricPlus$forceVersion, mixinServerInfo::viaFabricPlus$forcedVersion));
        }).size(98, 20);

        this.addDrawableChild(GeneralSettings.withOrientation(buttonBuilder, buttonPosition, width, height).build());
    }

}
