package com.viaversion.viafabricplus.protocoltranslator.impl.provider.vialegacy;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

import com.viaversion.viafabricplus.protocoltranslator.translator.ItemTranslator;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import net.raphimc.vialegacy.protocol.alpha.a1_2_3_5_1_2_6tob1_0_1_1_1.provider.AlphaInventoryProvider;

import java.util.List;

import static net.raphimc.vialegacy.protocol.alpha.a1_2_3_5_1_2_6tob1_0_1_1_1.Protocola1_2_3_5_1_2_6Tob1_0_1_1_1.copyItems;

public final class ViaFabricPlusAlphaInventoryProvider extends AlphaInventoryProvider {

    @Override
    public boolean usesInventoryTracker() {
        return false;
    }

    private Item[] getMinecraftContainerItems(final List<ItemStack> trackingItems) {
        final Item[] items = new Item[trackingItems.size()];

        for (int i = 0; i < items.length; i++) {
            final ItemStack alphaItem = trackingItems.get(i);
            if (alphaItem.isEmpty()) continue;

            items[i] = ItemTranslator.mcToVia(alphaItem, LegacyProtocolVersion.b1_8tob1_8_1);
        }
        return copyItems(items);
    }

    @Override
    public Item[] getMainInventoryItems(UserConnection connection) {
        if (getPlayer() == null) {
            return new Item[37];
        } else {
            return getMinecraftContainerItems(getPlayer().getInventory().main);
        }
    }

    @Override
    public Item[] getCraftingInventoryItems(UserConnection connection) {
        if (getPlayer() == null) {
            return new Item[4];
        } else {
            return getMinecraftContainerItems(getPlayer().playerScreenHandler.getCraftingInput().getHeldStacks());
        }
    }

    @Override
    public Item[] getArmorInventoryItems(UserConnection connection) {
        if (getPlayer() == null) {
            return new Item[4];
        } else {
            return getMinecraftContainerItems(getPlayer().getInventory().armor);
        }
    }

    @Override
    public Item[] getContainerItems(UserConnection connection) {
        if (getPlayer() == null) {
            return new Item[37];
        } else {
            return getMinecraftContainerItems(getPlayer().currentScreenHandler.getStacks());
        }
    }

    @Override
    public void addToInventory(UserConnection connection, Item item) {
        getPlayer().getInventory().insertStack(ItemTranslator.viaToMc(item, LegacyProtocolVersion.b1_8tob1_8_1));
    }

    private ClientPlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }

}
