package com.viaversion.viafabricplus.injection.mixin.base.ui;

import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.session.Session;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viaversion.viafabricplus.injection.access.base.IServerInfo;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viafabricplus.protocoltranslator.impl.provider.vialegacy.ViaFabricPlusClassicMPPassProvider;
import com.viaversion.viafabricplus.protocoltranslator.util.ProtocolVersionDetector;
import com.viaversion.viafabricplus.save.SaveManager;
import com.viaversion.viafabricplus.settings.impl.AuthenticationSettings;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.classic4j.model.classicube.account.CCAccount;
import io.netty.channel.ChannelFuture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.InetSocketAddress;

@Mixin(targets = "net.minecraft.client.gui.screen.multiplayer.ConnectScreen$1")
public abstract class MixinConnectScreen_1 {

    @Shadow
    @Final
    ServerInfo field_40415;

    @Final
    @Shadow
    ServerAddress field_33737;

    @Shadow
    @Final
    ConnectScreen field_2416;

    @Unique
    private boolean viaFabricPlus$useClassiCubeAccount;

    @WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;connect(Ljava/net/InetSocketAddress;ZLnet/minecraft/network/ClientConnection;)Lio/netty/channel/ChannelFuture;"))
    private ChannelFuture setServerInfoAndHandleDisconnect(InetSocketAddress address, boolean useEpoll, ClientConnection connection, Operation<ChannelFuture> original) throws Exception {
        final IServerInfo mixinServerInfo = (IServerInfo) this.field_40415;

        ProtocolVersion targetVersion = ProtocolTranslator.getTargetVersion();
        if (mixinServerInfo.viaFabricPlus$forcedVersion() != null && !mixinServerInfo.viaFabricPlus$passedDirectConnectScreen()) {
            targetVersion = mixinServerInfo.viaFabricPlus$forcedVersion();
            mixinServerInfo.viaFabricPlus$passDirectConnectScreen(false);
        }
        if (targetVersion == ProtocolTranslator.AUTO_DETECT_PROTOCOL) {
            this.field_2416.setStatus(Text.translatable("base.viafabricplus.detecting_server_version"));
            targetVersion = ProtocolVersionDetector.get(field_33737, address, ProtocolTranslator.NATIVE_VERSION);
        }
        ProtocolTranslator.setTargetVersion(targetVersion, true);

        this.viaFabricPlus$useClassiCubeAccount = AuthenticationSettings.INSTANCE.setSessionNameToClassiCubeNameInServerList.getValue() && ViaFabricPlusClassicMPPassProvider.classicubeMPPass != null;

        final ChannelFuture future = original.call(address, useEpoll, connection);
        ProtocolTranslator.injectPreviousVersionReset(future.channel());

        return future;
    }

    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/session/Session;getUsername()Ljava/lang/String;"))
    private String useClassiCubeUsername(Session instance) {
        if (this.viaFabricPlus$useClassiCubeAccount) {
            final CCAccount account = SaveManager.INSTANCE.getAccountsSave().getClassicubeAccount();
            if (account != null) {
                return account.username();
            }
        }
        return instance.getUsername();
    }

}
