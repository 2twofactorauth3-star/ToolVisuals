package com.viaversion.viafabricplus.injection.mixin.features.interaction.container_clicking;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.viaversion.viafabricplus.injection.access.base.IClientConnection;
import com.viaversion.viafabricplus.injection.access.interaction.container_clicking.IScreenHandler;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viafabricplus.protocoltranslator.translator.ItemTranslator;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ServerboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.Protocol1_16_4To1_17;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@SuppressWarnings("DataFlowIssue")
@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Final
    private ClientPlayNetworkHandler networkHandler;

    @Unique
    private ItemStack viaFabricPlus$oldCursorStack;

    @Unique
    private List<ItemStack> viaFabricPlus$oldItems;

    @ModifyVariable(method = "clickSlot", at = @At(value = "STORE"), ordinal = 0)
    private List<ItemStack> captureOldItems(List<ItemStack> oldItems) {
        viaFabricPlus$oldCursorStack = client.player.currentScreenHandler.getCursorStack().copy();
        return this.viaFabricPlus$oldItems = oldItems;
    }

    @WrapWithCondition(method = "clickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    private boolean handleWindowClick1_16_5(ClientPlayNetworkHandler instance, Packet<?> packet) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_16_4) && packet instanceof ClickSlotC2SPacket clickSlot) {
            ItemStack slotItemBeforeModification;
            if (this.viaFabricPlus$shouldBeEmpty(clickSlot.getActionType(), clickSlot.getSlot())) {
                slotItemBeforeModification = ItemStack.EMPTY;
            } else if (clickSlot.getSlot() < 0 || clickSlot.getSlot() >= viaFabricPlus$oldItems.size()) {
                slotItemBeforeModification = viaFabricPlus$oldCursorStack;
            } else {
                slotItemBeforeModification = viaFabricPlus$oldItems.get(clickSlot.getSlot());
            }

            final PacketWrapper containerClick = PacketWrapper.create(ServerboundPackets1_16_2.CONTAINER_CLICK, ((IClientConnection) networkHandler.getConnection()).viaFabricPlus$getUserConnection());
            containerClick.write(Types.BYTE, (byte) clickSlot.getSyncId());
            containerClick.write(Types.SHORT, (short) clickSlot.getSlot());
            containerClick.write(Types.BYTE, (byte) clickSlot.getButton());
            containerClick.write(Types.SHORT, ((IScreenHandler) client.player.currentScreenHandler).viaFabricPlus$incrementAndGetActionId());
            containerClick.write(Types.VAR_INT, clickSlot.getActionType().ordinal());
            containerClick.write(Types.ITEM1_13_2, ItemTranslator.mcToVia(slotItemBeforeModification, ProtocolVersion.v1_16_4));
            containerClick.scheduleSendToServer(Protocol1_16_4To1_17.class);

            viaFabricPlus$oldCursorStack = null;
            viaFabricPlus$oldItems = null;
            return false;
        }

        return true;
    }

    @Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
    private void removeClickActions(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.b1_5tob1_5_2) && !actionType.equals(SlotActionType.PICKUP)) {
            ci.cancel();
        } else if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.r1_4_6tor1_4_7) && !actionType.equals(SlotActionType.PICKUP) && !actionType.equals(SlotActionType.QUICK_MOVE) && !actionType.equals(SlotActionType.SWAP) && !actionType.equals(SlotActionType.CLONE)) {
            ci.cancel();
        }
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_15_2) && actionType == SlotActionType.SWAP && button == 40) {
            ci.cancel();
        }
    }

    @Unique
    private boolean viaFabricPlus$shouldBeEmpty(final SlotActionType type, final int slot) {

        if (type == SlotActionType.QUICK_CRAFT) return true;

        if (type == SlotActionType.THROW) return true;

        if (type == SlotActionType.QUICK_MOVE && ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_11_1))
            return true;

        return type == SlotActionType.PICKUP && slot == -999;
    }

}
