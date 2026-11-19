package ru.he3hauka.toolvisuals.mixin.optimization.ticksync;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.optimization.ticksync.TickSync;

@Mixin(ClientPacketListener.class)
public abstract class TickSyncPacketMixin {
    @Inject(method="handleMoveEntity",at=@At("HEAD"))
    private void toolvisuals$packet(ClientboundMoveEntityPacket packet,CallbackInfo callback){TickSync.packet(this);}
}
