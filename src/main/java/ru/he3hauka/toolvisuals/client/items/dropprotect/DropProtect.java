package ru.he3hauka.toolvisuals.client.items.dropprotect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.chat.message.ChatMessages;
import ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.DROP_PROTECT;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class DropProtect {
    private record Attempt(ItemStack stack, Screen screen, AbstractContainerMenu menu, Slot slot, int index,
                           boolean cursor, boolean entire, Object level, Object connection, long revision) {
        boolean current(Minecraft client) {
            if (client.player == null || !client.player.isAlive() || client.player.isSpectator()
                    || client.gui.screen() != screen || client.level != level || client.getConnection() != connection
                    || DropProtect.menu(client) != menu || ToolVisualsClient.CONFIG.revision() != revision) return false;
            var inventory = client.player.getInventory();
            ItemStack current = cursor ? menu.getCarried() : slot != null ? slot.getItem() : inventory.getSelectedItem();
            return (screen != null || inventory.getSelectedSlot() == index) && ItemStack.matches(stack, current);
        }
    }

    private static final DropConfirmation<Attempt> CONFIRMATION = new DropConfirmation<>();

    private DropProtect() {}

    public static boolean world(LocalPlayer player, boolean entire) {
        long input = DropInput.worldPress();
        var client = Minecraft.getInstance();
        if (client.player != player || client.gui.screen() != null) return false;
        return protect(client, player.getInventory().getSelectedItem(), null, player.getInventory().getSelectedSlot(),
                false, entire, input, client.options.keyDrop.getTranslatedKeyMessage());
    }

    public static boolean inventory(Screen screen, Slot slot, int index, int button, ContainerInput action) {
        var client = Minecraft.getInstance();
        if (client.player == null || client.gui.screen() != screen) return false;
        var menu = menu(client);
        boolean cursor = index == -999 && action == ContainerInput.PICKUP && !menu.getCarried().isEmpty();
        if (!cursor && (action != ContainerInput.THROW || slot == null || !slot.hasItem()
                || !menu.getCarried().isEmpty() || !slot.mayPickup(client.player))) return false;
        if (!ToolVisualsClient.CONFIG.flag(DROP_INVENTORY)) { CONFIRMATION.clear(); return false; }
        return protect(client, cursor ? menu.getCarried() : slot.getItem(), slot, index, cursor,
                cursor ? button == 0 : button == 1, DropInput.inventoryPress(cursor),
                cursor ? Component.translatable(button == 0 ? "key.mouse.left" : "key.mouse.right") : client.options.keyDrop.getTranslatedKeyMessage());
    }

    private static boolean protect(Minecraft client, ItemStack stack, Slot slot, int index, boolean cursor,
                                   boolean entire, long input, Component key) {
        var config = ToolVisualsClient.CONFIG;
        if (client.player == null || !client.player.isAlive() || client.player.isSpectator() || !config.active(DROP_PROTECT)
                || entire && !config.flag(DROP_ENTIRE_STACK) || !DropCategory.protects(config, stack)) {
            CONFIRMATION.clear();
            return false;
        }
        Attempt pending = CONFIRMATION.pending();
        if (pending == null || !pending.current(client) || pending.slot != slot || pending.index != index
                || pending.cursor != cursor || pending.entire != entire)
            pending = new Attempt(stack.copy(), client.gui.screen(), menu(client), slot, index,
                    cursor, entire, client.level, client.getConnection(), config.revision());
        var decision = CONFIRMATION.attempt(pending, input, System.nanoTime(), (int)config.value(DROP_CONFIRMATIONS),
                (long)(config.value(DROP_WINDOW) * 1_000_000_000L));
        if (decision.notifyPlayer()) ChatMessages.notice("drop_protect.confirm", stack.getHoverName(), key,
                Integer.toString(decision.remaining()), Integer.toString((int)config.value(DROP_WINDOW)));
        if (ModuleLog.active()) ModuleLog.action(DROP_PROTECT, decision.allowed() ? "drop_confirmed" : "drop_blocked",
                "slot=" + index + " inventory=" + (client.gui.screen() != null) + " entire=" + entire + " remaining=" + decision.remaining());
        return !decision.allowed();
    }

    public static void tick(Minecraft client) {
        DropInput.synchronize(client);
        Attempt pending = CONFIRMATION.pending();
        if (pending != null && (!pending.current(client) || !ToolVisualsClient.CONFIG.active(DROP_PROTECT)
                || CONFIRMATION.expired(System.nanoTime()))) CONFIRMATION.clear();
    }

    private static AbstractContainerMenu menu(Minecraft client) {
        return client.gui.screen() instanceof AbstractContainerScreen<?> container ? container.getMenu() : client.player.containerMenu;
    }
}
