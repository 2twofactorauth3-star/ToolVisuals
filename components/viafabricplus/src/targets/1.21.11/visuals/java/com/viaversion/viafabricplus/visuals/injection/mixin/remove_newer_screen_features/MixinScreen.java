package com.viaversion.viafabricplus.visuals.injection.mixin.remove_newer_screen_features;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class MixinScreen {

    @Final
    @Shadow
    @Nullable
    protected Minecraft minecraft;

    @Inject(method = "addRenderableWidget", at = @At("HEAD"), cancellable = true)
    private <T extends GuiEventListener & Renderable & NarratableEntry> void removeRecipeBook(T drawableElement, CallbackInfoReturnable<T> cir) {
        if (drawableElement instanceof ImageButton button && button.sprites == RecipeBookComponent.RECIPE_BUTTON_SPRITES) {
            final boolean furnace = ((Screen) (Object) this) instanceof FurnaceScreen;

            if (VisualSettings.INSTANCE.hideFurnaceRecipeBook.isEnabled() && furnace) {
                cir.setReturnValue(drawableElement);
            } else if (VisualSettings.INSTANCE.hideCraftingRecipeBook.isEnabled() && !furnace) {
                cir.setReturnValue(drawableElement);
            }
        }
    }

}
