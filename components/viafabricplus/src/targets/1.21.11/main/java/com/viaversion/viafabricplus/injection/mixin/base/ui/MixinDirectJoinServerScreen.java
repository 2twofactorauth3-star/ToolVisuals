package com.viaversion.viafabricplus.injection.mixin.base.ui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.Builder;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import com.viaversion.viafabricplus.settings.impl.GeneralSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.protocols.client.ProtocolMenus;

@Mixin(DirectJoinServerScreen.class)
public abstract class MixinDirectJoinServerScreen extends Screen {
   public MixinDirectJoinServerScreen(Component title) {
      super(title);
   }

   @Inject(method = "init", at = @At("RETURN"))
   private void addProtocolSelectionButton(CallbackInfo ci) {
      int buttonPosition = GeneralSettings.INSTANCE.directConnectScreenButtonOrientation.getIndex();
      if (buttonPosition != 0) {
         Builder builder = Button.builder(Component.nullToEmpty("ViaFabricPlus"), button -> {
            Object var10001 = null;
            ProtocolMenus.open(this);
         }).size(98, 20);
         GeneralSettings.setOrientation(builder::pos, buttonPosition, this.width, this.height);
         this.addRenderableWidget(builder.build());
      }
   }
}
