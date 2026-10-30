package com.viaversion.viafabricplus.injection.mixin.base.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viaversion.viafabricplus.injection.access.base.IServerData;
import com.viaversion.viafabricplus.settings.impl.GeneralSettings;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
public abstract class MixinServerSelectionList_OnlineServerEntry {

    @Shadow
    @Final
    private ServerData serverData;

    @WrapOperation(method = "renderContent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;setTooltipForNextFrame(Lnet/minecraft/network/chat/Component;II)V"))
    private void drawTranslatingState(GuiGraphics instance, Component text, int x, int y, Operation<Void> original) {
        final List<Component> tooltips = new ArrayList<>();
        tooltips.add(text);
        if (GeneralSettings.INSTANCE.showAdvertisedServerVersion.getValue()) {
            final ProtocolVersion version = ((IServerData) serverData).viaFabricPlus$translatingVersion();
            if (version != null) {
                tooltips.add(Component.translatable("base.viafabricplus.via_translates_to", version.getName() + " (" + version.getOriginalVersion() + ")"));
                tooltips.add(Component.translatable("base.viafabricplus.server_version", serverData.version.getString() + " (" + serverData.protocol + ")"));
            }
        }
        instance.setTooltipForNextFrame(Lists.transform(tooltips, Component::getVisualOrderText), x, y);
    }

}
