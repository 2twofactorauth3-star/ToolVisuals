package com.viaversion.viafabricplus.injection.mixin.features.networking.disable_server_pinging;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.viaversion.viafabricplus.injection.access.base.IServerData;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viafabricplus.settings.impl.DebugSettings;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
public abstract class MixinServerSelectionList_OnlineServerEntry {

    @Shadow
    @Final
    private ServerData serverData;

    @Mutable
    @Shadow
    @Final
    private FaviconTexture icon;

    @Unique
    private boolean viaFabricPlus$disableServerPinging = false;

    @WrapOperation(method = "renderContent", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/ThreadPoolExecutor;submit(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;"))
    private Future<?> disableServerPinging(ThreadPoolExecutor instance, Runnable runnable, Operation<Future<?>> original) {
        ProtocolVersion version = ((IServerData) serverData).viaFabricPlus$forcedVersion();
        if (version == null) {
            version = ProtocolTranslator.getTargetVersion();
        }

        viaFabricPlus$disableServerPinging = DebugSettings.INSTANCE.disableServerPinging.isEnabled(version);
        if (viaFabricPlus$disableServerPinging) {
            this.serverData.version = Component.nullToEmpty(version.getName());
            return null;
        }
        return original.call(instance, runnable);
    }

    @Redirect(method = "renderContent", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/ServerData$State;INCOMPATIBLE:Lnet/minecraft/client/multiplayer/ServerData$State;"))
    private ServerData.State disableServerPinging() {
        if (viaFabricPlus$disableServerPinging) {
            return this.serverData.state();
        } else {
            return ServerData.State.INCOMPATIBLE;
        }
    }

    @Redirect(method = "renderContent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;split(Lnet/minecraft/network/chat/FormattedText;I)Ljava/util/List;"))
    private List<FormattedCharSequence> disableServerPinging(Font instance, FormattedText text, int width) {
        if (viaFabricPlus$disableServerPinging) {
            return instance.split(Component.nullToEmpty(serverData.ip), width);
        } else {
            return instance.split(text, width);
        }
    }

    @ModifyArg(method = "renderContent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"), index = 2)
    private int disableServerPinging(int x) {
        if (viaFabricPlus$disableServerPinging) {
            x += 15   - 3  ;
        }
        return x;
    }

    @WrapWithCondition(method = "renderContent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0))
    private boolean disableServerPinging(GuiGraphics instance, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height) {
        return !viaFabricPlus$disableServerPinging;
    }

    @WrapWithCondition(method = "renderContent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;setTooltipForNextFrame(Ljava/util/List;II)V"))
    private boolean disableServerPinging(GuiGraphics instance, List<FormattedCharSequence> text, int x, int y) {
        return !viaFabricPlus$disableServerPinging;
    }

    @Redirect(method = "renderContent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/FaviconTexture;textureLocation()Lnet/minecraft/resources/Identifier;"))
    private Identifier disableServerPinging(FaviconTexture instance) {
        if (viaFabricPlus$disableServerPinging) {
            return FaviconTexture.MISSING_LOCATION;
        } else {
            return this.icon.textureLocation();
        }
    }

    @WrapWithCondition(method = "renderContent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;setTooltipForNextFrame(Lnet/minecraft/network/chat/Component;II)V"))
    private boolean disableServerPinging(GuiGraphics instance, Component text, int x, int y) {
        return !viaFabricPlus$disableServerPinging;
    }

}
