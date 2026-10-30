package ru.he3hauka.toolvisuals.mixin.gui;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureManager.class)
public abstract class FontSamplingMixin {
    @Inject(method="register",at=@At("TAIL"))
    private void toolvisuals$linearFonts(ResourceLocation id,AbstractTexture texture,CallbackInfo callback){if(id.getNamespace().equals("toolvisuals")&&texture instanceof FontTexture)texture.setFilter(true,false);}
}
