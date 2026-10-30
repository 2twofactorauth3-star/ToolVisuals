package ru.he3hauka.toolvisuals.mixin.items;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import ru.he3hauka.toolvisuals.client.items.ItemScroller;

@Mixin(AbstractContainerScreen.class)
public abstract class ItemScrollerMixin {
    @Unique private final ItemScroller toolvisuals$itemScroller = new ItemScroller();
    @Shadow private Slot getHoveredSlot(double x, double y) { throw new AssertionError(); }
    @Inject(method="mouseClicked",at=@At("HEAD"))
    private void toolvisuals$beginItemDrag(double x,double y,int button,CallbackInfoReturnable<Boolean> cir) {
        toolvisuals$itemScroller.release();
        if(ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.action(ru.he3hauka.toolvisuals.client.module.VisualModule.ITEM_SCROLLER,"mouse_press","button="+button+" shift_event="+net.minecraft.client.gui.screens.Screen.hasShiftDown());
    }
    @Inject(method="mouseScrolled",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$scrollItems(double x,double y,double horizontal,double vertical,CallbackInfoReturnable<Boolean> cir) {
        if(ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.action(ru.he3hauka.toolvisuals.client.module.VisualModule.ITEM_SCROLLER,"scroll_input","horizontal="+horizontal+" vertical="+vertical);
        if ((vertical != 0 || horizontal != 0) && Double.isFinite(vertical) && Double.isFinite(horizontal)
                && toolvisuals$itemScroller.move((AbstractContainerScreen<?>)(Object)this,getHoveredSlot(x,y),false)) cir.setReturnValue(true);
    }
    @Inject(method="mouseDragged",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$dragItems(double x,double y,int button,double dx,double dy,CallbackInfoReturnable<Boolean> cir) {
        if (button == InputConstants.MOUSE_BUTTON_LEFT && toolvisuals$itemScroller.move((AbstractContainerScreen<?>)(Object)this,getHoveredSlot(x,y),true)) cir.setReturnValue(true);
    }
    @Inject(method="mouseReleased",at=@At("HEAD"))
    private void toolvisuals$endItemDrag(double x,double y,int button,CallbackInfoReturnable<Boolean> cir) {
        if(button == InputConstants.MOUSE_BUTTON_LEFT)toolvisuals$itemScroller.release();
    }
    @Inject(method="slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V",at=@At("HEAD"))
    private void toolvisuals$rememberQuickMove(Slot slot,int index,int button,ContainerInput input,CallbackInfo ci) {
        if(input == ContainerInput.QUICK_MOVE && slot != null){
            toolvisuals$itemScroller.mark(slot.index);
            if(ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.action(ru.he3hauka.toolvisuals.client.module.VisualModule.ITEM_SCROLLER,"native_quick_move","slot="+slot.index+" button="+button);
        }
    }
    @Inject(method="removed",at=@At("HEAD"))
    private void toolvisuals$clearItemDrag(CallbackInfo ci) { toolvisuals$itemScroller.release(); }
}
