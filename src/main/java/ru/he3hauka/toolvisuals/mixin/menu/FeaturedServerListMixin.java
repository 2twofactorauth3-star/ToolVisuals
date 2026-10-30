package ru.he3hauka.toolvisuals.mixin.menu;

import java.util.List;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.connection.featured.FeaturedServer;

@Mixin(ServerList.class)
public abstract class FeaturedServerListMixin {
    @Shadow @Final private List<ServerData> serverList;
    @Shadow @Final private List<ServerData> hiddenServerList;
    @Shadow public abstract void save();

    @Inject(method = "load", at = @At("RETURN"))
    private void toolvisuals$restore(CallbackInfo ci) {
        if (FeaturedServer.ensure(serverList, hiddenServerList)) save();
    }
    @Inject(method = "save", at = @At("HEAD"))
    private void toolvisuals$keepFirst(CallbackInfo ci) { FeaturedServer.ensure(serverList, hiddenServerList); }
    @Inject(method = "add", at = @At("RETURN"))
    private void toolvisuals$deduplicate(ServerData data, boolean hidden, CallbackInfo ci) {
        FeaturedServer.ensure(serverList, hiddenServerList);
    }
    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$protectRemoval(ServerData data, CallbackInfo ci) {
        if (FeaturedServer.matches(data)) ci.cancel();
    }
    @Inject(method = "swap", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$protectOrder(int first, int second, CallbackInfo ci) {
        if (!FeaturedServer.canSwap(first, second)) ci.cancel();
    }
    @Inject(method = "replace", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$protectEdit(int index, ServerData data, CallbackInfo ci) {
        if (index == 0) ci.cancel();
    }
}
