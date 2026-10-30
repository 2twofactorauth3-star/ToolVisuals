package com.viaversion.viafabricplus.injection.mixin.base.ui;

import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viaversion.viafabricplus.injection.access.base.IServerInfo;
import com.viaversion.viafabricplus.settings.impl.GeneralSettings;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(MultiplayerServerListWidget.ServerEntry.class)
public abstract class MixinMultiplayerServerListWidget_ServerEntry {

    @Shadow
    @Final
    private ServerInfo server;

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;setTooltip(Lnet/minecraft/text/Text;)V"))
    private void drawTranslatingState(MultiplayerScreen instance, Text text, Operation<Void> original) {
        final List<Text> tooltips = new ArrayList<>();
        tooltips.add(text);
        if (GeneralSettings.INSTANCE.showAdvertisedServerVersion.getValue()) {
            final ProtocolVersion version = ((IServerInfo) server).viaFabricPlus$translatingVersion();
            if (version != null) {
                tooltips.add(Text.translatable("base.viafabricplus.via_translates_to", version.getName() + " (" + version.getOriginalVersion() + ")"));
                tooltips.add(Text.translatable("base.viafabricplus.server_version", server.version.getString() + " (" + server.protocolVersion + ")"));
            }
        }
        instance.setTooltip(Lists.transform(tooltips, Text::asOrderedText));
    }

}
