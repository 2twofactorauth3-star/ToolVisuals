package ru.he3hauka.toolvisuals.mixin.hud;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.optimization.hud.PlayerRoster;

@Mixin(ClientPacketListener.class)
public abstract class PlayerRosterMixin {
    @Inject(method="handlePlayerInfoUpdate",at=@At("TAIL"))
    private void toolvisuals$info(ClientboundPlayerInfoUpdatePacket packet,CallbackInfo ci){PlayerRoster.info(packet);}
    @Inject(method="handlePlayerInfoRemove",at=@At("TAIL"))
    private void toolvisuals$remove(ClientboundPlayerInfoRemovePacket packet,CallbackInfo ci){PlayerRoster.membershipChanged();}
    @Inject(method={"handleSetScore","handleResetScore","handleSetDisplayObjective","handleSetPlayerTeamPacket","handleAddObjective"},at=@At("TAIL"))
    private void toolvisuals$presentation(CallbackInfo ci){PlayerRoster.presentationChanged();}
}
