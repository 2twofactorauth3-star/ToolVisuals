package ru.he3hauka.toolvisuals.mixin.auction;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.auction.AuctionHelper;

@Mixin(MultiPlayerGameMode.class)
public abstract class AuctionGameModeMixin {
    @Inject(method="handleContainerInput",at=@At("HEAD"))
    private void toolvisuals$recordAuctionClick(int container,int slot,int button,ContainerInput input,Player player,CallbackInfo ci) {
        AuctionHelper.playerClick(container,slot,button,input);
        ru.he3hauka.toolvisuals.client.items.ElytraSwap.playerClick();
    }
}
