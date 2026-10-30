package ru.he3hauka.toolvisuals.client.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.player.ClientActions;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.ELYTRA_SWAP;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.ELYTRA_SWAP_CLOSE;

public final class ElytraSwap {
    private static int[] steps;
    private static int step, hotbar, candidate, chest;
    private static boolean opened, internal;
    private static Object connection;
    private static Screen screen;
    private static ItemStack expectedChest, expectedOther;
    private ElytraSwap() {}
    public static boolean running() { return steps != null; }
    public static void request(Minecraft client) {
        if (running() || !ToolVisualsClient.CONFIG.active(ELYTRA_SWAP) || client.player == null || client.gameMode == null) return;
        var player = client.player; var menu = player.inventoryMenu;
        if (player.containerMenu != menu || !menu.getCarried().isEmpty() || player.isSpectator()
                || client.gui.screen() != null && !(client.gui.screen() instanceof InventoryScreen)) { notice("busy"); return; }
        chest = InventoryMenu.ARMOR_SLOT_START + 1;
        var worn = menu.getSlot(chest);
        if (!worn.mayPickup(player)) { notice("blocked"); return; }
        boolean needChest = worn.getItem().is(Items.ELYTRA);
        candidate = -1;
        for (int i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.USE_ROW_SLOT_END; i++) {
            var slot = menu.getSlot(i); var stack = slot.getItem();
            if ((needChest ? stack.is(ItemTags.CHEST_ARMOR) : stack.is(Items.ELYTRA)) && slot.mayPickup(player) && worn.mayPlace(stack)) { candidate = i; break; }
        }
        if (candidate < 0) { notice(needChest ? "no_chest" : "no_elytra"); return; }
        expectedChest = menu.getSlot(candidate).getItem().copy(); expectedOther = worn.getItem().copy();
        hotbar = 0;
        for (int i = 0; i < 9; i++) if (player.getInventory().getItem(i).isEmpty()) { hotbar = i; break; }
        boolean inHotbar = InventoryMenu.isHotbarSlot(candidate);
        if (inHotbar) hotbar = menu.getSlot(candidate).getContainerSlot();
        steps = inHotbar ? new int[]{chest} : new int[]{candidate,chest,candidate}; step = 0;
        if(ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.action(ELYTRA_SWAP,"started","steps="+steps.length+" candidate_slot="+candidate);
        connection = client.getConnection(); opened = client.gui.screen() == null;
        screen = client.gui.screen() instanceof InventoryScreen inventory ? inventory : new InventoryScreen(player);
        if (opened) { client.gui.setScreen(screen); screen = client.gui.screen(); }
    }
    public static void tick(Minecraft client) {
        if (!running()) return;
        if (client.player == null || client.gameMode == null || client.getConnection() != connection
                || !ToolVisualsClient.CONFIG.active(ELYTRA_SWAP) || client.gui.screen() != screen
                || client.player.containerMenu != client.player.inventoryMenu || !client.player.inventoryMenu.getCarried().isEmpty()) { cancel(); return; }
        var menu = client.player.inventoryMenu;
        if (step < steps.length) {
            internal = true;
            try { client.gameMode.handleContainerInput(menu.containerId,steps[step++],hotbar,ContainerInput.SWAP,client.player); }
            finally { internal = false; }
        } else {
            boolean success = ItemStack.matches(expectedChest,menu.getSlot(chest).getItem()) && ItemStack.matches(expectedOther,menu.getSlot(candidate).getItem());
            if(ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.action(ELYTRA_SWAP,"finished","matched_prediction="+success);
            cancel();
            if (!success) notice("rejected");
            else if (opened && ToolVisualsClient.CONFIG.flag(ELYTRA_SWAP_CLOSE) && client.gui.screen() == screen) client.player.closeContainer();
        }
    }
    public static void playerClick() { if (!internal) cancel(); }
    private static void cancel() { steps = null; connection = null; }
    private static void notice(String key) { ClientActions.notice("elytra_swap." + key); }
}
