package com.viaversion.viafabricplus.screen.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viafabricplus.screen.VFPList;
import com.viaversion.viafabricplus.screen.VFPListEntry;
import com.viaversion.viafabricplus.screen.VFPScreen;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.awt.Color;
import java.util.function.Consumer;
import java.util.function.Supplier;
import ru.he3hauka.toolvisuals.client.connection.ProtocolPolicy;

public final class PerServerVersionScreen extends VFPScreen {
   private final Consumer<ProtocolVersion> selectionConsumer;
   private final Supplier<ProtocolVersion> selectionSupplier;

   public PerServerVersionScreen(Screen prevScreen, Consumer<ProtocolVersion> selectionConsumer, Supplier<ProtocolVersion> selectionSupplier) {
      super(Component.translatable("screen.viafabricplus.force_version"), false);
      this.prevScreen = prevScreen;
      this.selectionConsumer = selectionConsumer;
      this.selectionSupplier = selectionSupplier;
      this.setupSubtitle(Component.translatable("force_version.viafabricplus.title"));
   }

   @Override
   protected void init() {
      super.init();
      this.addRenderableWidget(new PerServerVersionScreen.SlotList(this.minecraft, this.width, this.height, 6 + (9 + 2) * 3, -5, 9 + 4));
   }

   public final class ProtocolSlot extends PerServerVersionScreen.SharedSlot {
      private final ProtocolVersion protocolVersion;

      public ProtocolSlot(final ProtocolVersion protocolVersion) {
         this.protocolVersion = protocolVersion;
      }

      public Component getNarration() {
         return Component.nullToEmpty(this.protocolVersion.getName());
      }

      @Override
      public void mappedMouseClicked(double mouseX, double mouseY, int button) {
         PerServerVersionScreen.this.selectionConsumer.accept(this.protocolVersion);
      }

      @Override
      public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
         boolean isSelected = this.protocolVersion.equals(PerServerVersionScreen.this.selectionSupplier.get());
         Font textRenderer = Minecraft.getInstance().font;
         context.drawCenteredString(
            textRenderer, this.protocolVersion.getName(), this.getContentXMiddle(), this.getContentYMiddle() - 9 / 2, isSelected ? Color.GREEN.getRGB() : -1
         );
      }
   }

   public final class ResetSlot extends PerServerVersionScreen.SharedSlot {
      public Component getNarration() {
         return Component.translatable("base.viafabricplus.cancel_and_reset");
      }

      @Override
      public void mappedMouseClicked(double mouseX, double mouseY, int button) {
         PerServerVersionScreen.this.selectionConsumer.accept(null);
      }

      @Override
      public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
         Font textRenderer = Minecraft.getInstance().font;
         context.drawCenteredString(
            textRenderer, ((MutableComponent)this.getNarration()).withStyle(ChatFormatting.GOLD), this.getContentXMiddle(), this.getContentYMiddle() - 9 / 2, -1
         );
      }
   }

   public abstract class SharedSlot extends VFPListEntry {
      @Override
      public void mappedMouseClicked(double mouseX, double mouseY, int button) {
         PerServerVersionScreen.this.onClose();
      }
   }

   public final class SlotList extends VFPList {
      public SlotList(Minecraft minecraftClient, int width, int height, int top, int bottom, int entryHeight) {
         super(minecraftClient, width, height, top, bottom, entryHeight);
         this.addEntry(PerServerVersionScreen.this.new ResetSlot());
         ProtocolPolicy.catalog(ProtocolVersion.getReversedProtocols(), ProtocolTranslator.NATIVE_VERSION)
            .stream()
            .map(x$0 -> PerServerVersionScreen.this.new ProtocolSlot(x$0))
            .forEach(x$0 -> this.addEntry(x$0));
      }
   }
}
