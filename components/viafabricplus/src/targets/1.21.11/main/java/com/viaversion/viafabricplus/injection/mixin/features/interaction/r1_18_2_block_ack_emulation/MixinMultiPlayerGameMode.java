package com.viaversion.viafabricplus.injection.mixin.features.interaction.r1_18_2_block_ack_emulation;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;

import com.viaversion.viafabricplus.features.interaction.r1_18_2_block_ack_emulation.ClientPlayerInteractionManager1_18_2;
import com.viaversion.viafabricplus.injection.access.interaction.r1_18_2_block_ack_emulation.IMultiPlayerGameMode;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode implements IMultiPlayerGameMode {

    @Unique
    private final ClientPlayerInteractionManager1_18_2 viaFabricPlus$1_18_2InteractionManager = new ClientPlayerInteractionManager1_18_2();

    @Inject(method = "startPrediction", at = @At("HEAD"))
    private void trackPlayerAction(ClientLevel world, PredictiveAction packetCreator, CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().betweenInclusive(ProtocolVersion.v1_14_4, ProtocolVersion.v1_18_2) && packetCreator instanceof ServerboundPlayerActionPacket playerActionC2SPacket) {
            this.viaFabricPlus$1_18_2InteractionManager.trackPlayerAction(playerActionC2SPacket.getAction(), playerActionC2SPacket.getPos());
        }
    }

    @Redirect(method = {"startDestroyBlock", "stopDestroyBlock"}, at = @At(value = "NEW", target = "(Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket$Action;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket;"))
    private ServerboundPlayerActionPacket trackPlayerAction(ServerboundPlayerActionPacket.Action action, BlockPos pos, Direction direction) {
        if (ProtocolTranslator.getTargetVersion().betweenInclusive(ProtocolVersion.v1_14_4, ProtocolVersion.v1_18_2)) {
            this.viaFabricPlus$1_18_2InteractionManager.trackPlayerAction(action, pos);
        }
        return new ServerboundPlayerActionPacket(action, pos, direction);
    }

    @Override
    public ClientPlayerInteractionManager1_18_2 viaFabricPlus$get1_18_2InteractionManager() {
        return this.viaFabricPlus$1_18_2InteractionManager;
    }

}
