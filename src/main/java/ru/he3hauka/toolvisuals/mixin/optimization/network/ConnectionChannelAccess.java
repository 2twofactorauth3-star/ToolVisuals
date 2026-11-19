package ru.he3hauka.toolvisuals.mixin.optimization.network;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Connection.class)
public interface ConnectionChannelAccess {
    @Accessor("channel") Channel toolvisuals$channel();
}
