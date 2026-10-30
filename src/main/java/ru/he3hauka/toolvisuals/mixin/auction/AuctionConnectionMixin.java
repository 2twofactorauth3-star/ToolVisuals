package ru.he3hauka.toolvisuals.mixin.auction;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.auction.AuctionHelper;

@Mixin(ClientPacketListener.class)
public abstract class AuctionConnectionMixin {
    @Inject(method="sendChat",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$localAuctionCommand(String text,CallbackInfo ci) {if(ru.he3hauka.toolvisuals.client.command.LocalCommands.handle(text))ci.cancel();}
    @Inject(method="sendCommand",at=@At("HEAD"))
    private void toolvisuals$recordAuctionCommand(String command,CallbackInfo ci) {AuctionHelper.outgoingCommand(command);}
    @Inject(method="handleContainerContent",at=@At("TAIL"))
    private void toolvisuals$auctionContents(ClientboundContainerSetContentPacket packet,CallbackInfo ci) {
        AuctionHelper.serverMenuResponse(packet.containerId(),false);
        if(ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.event("Inventory","server_contents","container="+packet.containerId());
    }
    @Inject(method="handleContainerSetSlot",at=@At("TAIL"))
    private void toolvisuals$auctionSlot(ClientboundContainerSetSlotPacket packet,CallbackInfo ci) {
        AuctionHelper.serverMenuResponse(packet.getContainerId(),false);
        if(ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.event("Inventory","server_slot","container="+packet.getContainerId()+" slot="+packet.getSlot());
    }
    @Inject(method="handleContainerClose",at=@At("TAIL"))
    private void toolvisuals$auctionClosed(ClientboundContainerClosePacket packet,CallbackInfo ci) {
        AuctionHelper.serverMenuResponse(packet.getContainerId(),false);
        if(ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.event("Inventory","closed","container="+packet.getContainerId());
    }
    @Inject(method="handleOpenScreen",at=@At("TAIL"))
    private void toolvisuals$auctionOpened(ClientboundOpenScreenPacket packet,CallbackInfo ci) {
        AuctionHelper.serverMenuResponse(packet.getContainerId(),true);
        if(ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.event("Inventory","opened","container="+packet.getContainerId());
    }
}
