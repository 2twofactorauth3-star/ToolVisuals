package ru.he3hauka.toolvisuals.mixin.replay;

import java.util.List;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundPlayerInfoUpdatePacket.class)
public interface ReplayPlayerInfoAccessor {
    @Mutable
    @Accessor("entries")
    void toolvisuals$replayEntries(List<ClientboundPlayerInfoUpdatePacket.Entry> entries);
}
