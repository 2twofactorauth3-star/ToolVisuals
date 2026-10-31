package ru.he3hauka.toolvisuals.mixin.replay;

import net.minecraft.core.Registry;
import net.minecraft.tags.TagNetworkSerialization;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TagNetworkSerialization.class)
public interface ReplayTagsInvoker {
    @Invoker("serializeToNetwork")
    static <T> TagNetworkSerialization.NetworkPayload toolvisuals$replayTags(Registry<T> registry) {
        throw new AssertionError();
    }
}
