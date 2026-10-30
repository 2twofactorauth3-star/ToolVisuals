package ru.he3hauka.toolvisuals.mixin.chunkanimator;

import java.nio.ByteBuffer;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import ru.he3hauka.toolvisuals.client.chunkanimator.ChunkAnimations;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.UniformBufferManager$GlobalUniforms", remap = false)
public abstract class ChunkAnimationUniformMixin {
    @WrapMethod(method = "write")
    private void toolvisuals$chunkAnimationUniforms(ByteBuffer buffer, Operation<Void> original) {
        int base = buffer.position();
        original.call(buffer);
        ChunkAnimations.writeUniforms(buffer, base);
        ru.he3hauka.toolvisuals.client.optimization.FastShaders.writeUniforms(buffer, base);
    }
}
