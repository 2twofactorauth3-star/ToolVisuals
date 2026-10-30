package com.viaversion.viafabricplus.visuals.injection.mixin.remove_newer_screen_features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.FurnaceScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class MixinScreen {

    @Shadow
    @Nullable
    protected MinecraftClient client;

    @Inject(method = "addDrawableChild", at = @At("HEAD"), cancellable = true)
    private <T extends Element & Drawable & Selectable> void removeRecipeBook(T drawableElement, CallbackInfoReturnable<T> cir) {
        if (drawableElement instanceof TexturedButtonWidget button && button.textures == RecipeBookWidget.BUTTON_TEXTURES) {
            final boolean furnace = ((Screen) (Object) this) instanceof FurnaceScreen;

            if (VisualSettings.INSTANCE.hideFurnaceRecipeBook.isEnabled() && furnace) {
                cir.setReturnValue(drawableElement);
            } else if (VisualSettings.INSTANCE.hideCraftingRecipeBook.isEnabled() && !furnace) {
                cir.setReturnValue(drawableElement);
            }
        }
    }

}
