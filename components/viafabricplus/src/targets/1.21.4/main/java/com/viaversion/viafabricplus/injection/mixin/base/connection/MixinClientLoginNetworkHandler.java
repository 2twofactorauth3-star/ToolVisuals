package com.viaversion.viafabricplus.injection.mixin.base.connection;

import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;

import com.viaversion.viafabricplus.injection.access.base.IClientConnection;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import net.raphimc.vialegacy.protocol.release.r1_6_4tor1_7_2_5.storage.ProtocolMetadataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("DataFlowIssue")
@Mixin(ClientLoginNetworkHandler.class)
public abstract class MixinClientLoginNetworkHandler {

    @Shadow
    @Final
    private ClientConnection connection;

    @Inject(method = "joinServerSession", at = @At("HEAD"), cancellable = true)
    public void onlyVerifySessionInOnlineMode(String serverId, CallbackInfoReturnable<Text> cir) {
        final IClientConnection mixinClientConnection = (IClientConnection) connection;
        if (mixinClientConnection.viaFabricPlus$getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.r1_6_4)) {

            if (!mixinClientConnection.viaFabricPlus$getUserConnection().get(ProtocolMetadataStorage.class).authenticate) {
                cir.setReturnValue(null);
            }
        }
    }

}
