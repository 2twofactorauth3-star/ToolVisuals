package ru.he3hauka.toolvisuals.mixin.crystals;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.optimization.crystal.CrystalOptimizer;

@Mixin(ClientPacketListener.class)
public abstract class CrystalConnectionMixin {
    @Inject(method="handleRemoveEntities",at=@At("TAIL"))
    private void toolvisuals$confirmedCrystals(ClientboundRemoveEntitiesPacket packet,CallbackInfo callback){for(int id:packet.entityIds())CrystalOptimizer.confirmed(id);}
    @Inject(method="handleAddEntity",at=@At("TAIL"))
    private void toolvisuals$replaceCrystalId(ClientboundAddEntityPacket packet,CallbackInfo callback){CrystalOptimizer.confirmed(packet.getId());}
}
