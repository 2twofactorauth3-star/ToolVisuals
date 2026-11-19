package ru.he3hauka.toolvisuals.mixin.optimization.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.VarInt;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import ru.he3hauka.toolvisuals.client.optimization.network.NetworkOptimizations;
import ru.he3hauka.toolvisuals.client.optimization.network.PacketBytes;

@Mixin(VarInt.class)
public abstract class VarIntMixin {
    @WrapMethod(method="getByteSize")
    private static int toolvisuals$size(int value,Operation<Integer> original){return NetworkOptimizations.enabled()?PacketBytes.size(value):original.call(value);}
    @WrapMethod(method="write")
    private static ByteBuf toolvisuals$write(ByteBuf buffer,int value,Operation<ByteBuf> original){return NetworkOptimizations.enabled()?PacketBytes.write(buffer,value):original.call(buffer,value);}
}
