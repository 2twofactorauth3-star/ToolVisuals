package com.viaversion.viafabricplus.injection.mixin.base.ui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.DirectConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.Builder;
import net.minecraft.text.Text;

import com.viaversion.viafabricplus.settings.impl.GeneralSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.protocols.client.ProtocolMenus;

@Mixin(DirectConnectScreen.class)
public abstract class MixinDirectConnectScreen extends Screen {
   public MixinDirectConnectScreen(Text title) {
      super(title);
   }

   @Inject(method = "init", at = @At("RETURN"))
   private void addProtocolSelectionButton(CallbackInfo ci) {
      int buttonPosition = GeneralSettings.INSTANCE.directConnectScreenButtonOrientation.getIndex();
      if (buttonPosition != 0) {
         Builder builder = ButtonWidget.builder(Text.of("ViaFabricPlus"), button -> {
            Object var10001 = null;
            ProtocolMenus.open(this);
         }).size(98, 20);
         this.addDrawableChild(GeneralSettings.withOrientation(builder, buttonPosition, this.width, this.height).build());
      }
   }
}
