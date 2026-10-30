package com.viaversion.viafabricplus.injection.mixin.base.ui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;

import com.viaversion.viafabricplus.settings.impl.GeneralSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.protocols.client.ProtocolMenus;

@Mixin(JoinMultiplayerScreen.class)
public abstract class MixinJoinMultiplayerScreen extends Screen {
   @Unique
   private Button viaFabricPlus$button;

   public MixinJoinMultiplayerScreen(Component title) {
      super(title);
   }

   @Inject(method = "repositionElements", at = @At("RETURN"))
   private void addProtocolSelectionButton(CallbackInfo ci) {
      int buttonPosition = GeneralSettings.INSTANCE.multiplayerScreenButtonOrientation.getIndex();
      if (buttonPosition != 0) {
         if (this.viaFabricPlus$button == null) {
            this.viaFabricPlus$button = Button.builder(Component.nullToEmpty("ViaFabricPlus"), button -> {
               Object var10001 = null;
               ProtocolMenus.open(this);
            }).size(98, 20).build();
            this.addRenderableWidget(this.viaFabricPlus$button);
         }

         GeneralSettings.setOrientation(this.viaFabricPlus$button::setPosition, buttonPosition, this.width, this.height);
      }
   }
}
