package ru.he3hauka.toolvisuals.mixin.session;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.session.SessionStats;

@Mixin(ClientPacketListener.class)
public abstract class SessionConnectionMixin {
    @Inject(method="handlePlayerCombatKill",at=@At("TAIL"))
    private void toolvisuals$countDeath(ClientboundPlayerCombatKillPacket packet,CallbackInfo callback){var player=Minecraft.getInstance().player;if(player!=null&&packet.playerId()==player.getId())SessionStats.death();}
}
