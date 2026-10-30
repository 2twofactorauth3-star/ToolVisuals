package com.viaversion.viafabricplus.injection.mixin.features.interaction.container_clicking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantMenu.class)
public abstract class MixinMerchantMenu extends AbstractContainerMenu {

    @Shadow
    @Final
    private MerchantContainer tradeContainer;

    @Shadow
    public abstract MerchantOffers getOffers();

    protected MixinMerchantMenu(@Nullable MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "tryMoveItems", at = @At("HEAD"), cancellable = true)
    private void onSwitchTo(int recipeId, CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
            ci.cancel();

            if (recipeId >= this.getOffers().size()) {
                return;
            }

            final MultiPlayerGameMode interactionManager = Minecraft.getInstance().gameMode;
            final LocalPlayer player = Minecraft.getInstance().player;

            if (!this.tradeContainer.getItem(0).isEmpty()) {
                final int count = this.tradeContainer.getItem(0).getCount();
                interactionManager.handleInventoryMouseClick(containerId, 0, 0, ClickType.QUICK_MOVE, player);
                if (count == this.tradeContainer.getItem(0).getCount()) {
                    return;
                }
            }

            if (!this.tradeContainer.getItem(1).isEmpty()) {
                final int count = this.tradeContainer.getItem(1).getCount();
                interactionManager.handleInventoryMouseClick(containerId, 1, 0, ClickType.QUICK_MOVE, player);
                if (count == this.tradeContainer.getItem(1).getCount()) {
                    return;
                }
            }

            if (this.tradeContainer.getItem(0).isEmpty() && this.tradeContainer.getItem(1).isEmpty()) {
                final MerchantOffer tradeOffer = this.getOffers().get(recipeId);
                this.viaFabricPlus$autofill(interactionManager, player, 0, tradeOffer.getItemCostA());
                tradeOffer.getItemCostB().ifPresent(item -> this.viaFabricPlus$autofill(interactionManager, player, 1, item));
            }
        }
    }

    @Inject(method = "canTakeItemForPickAll", at = @At("HEAD"), cancellable = true)
    private void modifyCanInsertIntoSlot(ItemStack stack, Slot slot, CallbackInfoReturnable<Boolean> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private void viaFabricPlus$autofill(MultiPlayerGameMode interactionManager, LocalPlayer player, int inputSlot, ItemCost stackNeeded) {
        int slot;
        for (slot = 3; slot < 39; slot++) {
            final ItemStack itemStack = this.slots.get(slot).getItem();
            if (!itemStack.isEmpty() && stackNeeded.test(itemStack)) {
                final ItemStack itemStack2 = this.tradeContainer.getItem(inputSlot);
                if (itemStack2.isEmpty() || ItemStack.isSameItemSameComponents(itemStack, itemStack2)) {
                    break;
                }
            }
        }
        if (slot == 39) {
            return;
        }

        final boolean wasHoldingItem = !player.containerMenu.getCarried().isEmpty();
        interactionManager.handleInventoryMouseClick(containerId, slot, 0, ClickType.PICKUP, player);
        interactionManager.handleInventoryMouseClick(containerId, slot, 0, ClickType.PICKUP_ALL, player);
        interactionManager.handleInventoryMouseClick(containerId, inputSlot, 0, ClickType.PICKUP, player);
        if (wasHoldingItem) interactionManager.handleInventoryMouseClick(containerId, slot, 0, ClickType.PICKUP, player);
    }

}
