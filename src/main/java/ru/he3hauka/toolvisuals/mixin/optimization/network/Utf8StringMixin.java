package ru.he3hauka.toolvisuals.mixin.optimization.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.Utf8String;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import ru.he3hauka.toolvisuals.client.optimization.network.NetworkOptimizations;
import ru.he3hauka.toolvisuals.client.optimization.network.PacketBytes;

@Mixin(Utf8String.class)
public abstract class Utf8StringMixin {
    @WrapMethod(method="write")
    private static void toolvisuals$write(ByteBuf buffer,CharSequence text,int maximum,Operation<Void> original){if(NetworkOptimizations.enabled())PacketBytes.utf8(buffer,text,maximum);else original.call(buffer,text,maximum);}
}
