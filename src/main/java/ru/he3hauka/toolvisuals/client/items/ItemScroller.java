package ru.he3hauka.toolvisuals.client.items;

import java.util.HashSet;
import java.util.Set;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.sdl.SDLKeyboard;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog;
import ru.he3hauka.toolvisuals.mixin.items.ContainerScreenInvoker;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.ITEM_SCROLLER;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.ITEM_SCROLLER_MODE;

public final class ItemScroller {
    private final Set<Integer> visited = new HashSet<>();

    public void release() { visited.clear(); }
    public boolean mark(int index) { return index >= 0 && visited.add(index); }

    public boolean move(AbstractContainerScreen<?> screen, Slot slot, boolean drag) {
        var client = Minecraft.getInstance();
        if (!ToolVisualsClient.CONFIG.active(ITEM_SCROLLER)) return rejected("module_disabled");
        if (client.player == null || client.gameMode == null) return rejected("no_player_or_game_mode");
        if (slot == null) return rejected("no_hovered_slot");
        if (!shiftDown()) return rejected("shift_not_held");
        if (!client.isWindowActive() || client.gui.overlay() != null) return rejected("window_or_overlay");
        double mode = ToolVisualsClient.CONFIG.value(ITEM_SCROLLER_MODE);
        var menu = screen.getMenu();
        if (mode == (drag ? 0 : 1)) return rejected(drag?"drag_disabled_by_mode":"scroll_disabled_by_mode");
        if (client.gui.screen() != screen || client.player.containerMenu != menu) return rejected("screen_or_menu_mismatch");
        if (!menu.getCarried().isEmpty()) return rejected("item_on_cursor");
        if (slot.index < 0 || slot.index >= menu.slots.size() || menu.slots.get(slot.index) != slot) return rejected("invalid_slot");
        if (!slot.isActive()) return rejected("inactive_slot");
        if (!slot.hasItem()) return rejected("empty_slot");
        if (!slot.mayPickup(client.player)) return rejected("pickup_not_allowed");
        if (drag && !mark(slot.index)) { ModuleLog.state(ITEM_SCROLLER,"decision","already_moved_in_gesture");return true; }
        try {
            ((ContainerScreenInvoker)screen).toolvisuals$slotClicked(slot,slot.index,0,ContainerInput.QUICK_MOVE);
        } catch(RuntimeException error) { ModuleLog.failure(ITEM_SCROLLER,"quick_move_failed",error);throw error; }
        ModuleLog.state(ITEM_SCROLLER,"decision","quick_move_requested");
        if(ModuleLog.active())ModuleLog.action(ITEM_SCROLLER,"quick_move_requested","slot="+slot.index+" container="+menu.containerId+" drag="+drag);
        return true;
    }

    private static boolean rejected(String reason) { ModuleLog.state(ITEM_SCROLLER,"decision",reason);return false; }

    private static boolean shiftDown() {
        boolean logical=(SDLKeyboard.SDL_GetModState() & InputConstants.MOD_SHIFT) != 0;
        boolean left=InputConstants.isKeyDown(InputConstants.KEY_LSHIFT),right=InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
        if(ModuleLog.active())ModuleLog.state(ITEM_SCROLLER,"shift","logical="+logical+" left="+left+" right="+right);
        return logical || left || right;
    }
}
