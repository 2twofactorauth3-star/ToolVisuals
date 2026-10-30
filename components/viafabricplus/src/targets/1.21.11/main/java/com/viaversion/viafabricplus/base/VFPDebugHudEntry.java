package com.viaversion.viafabricplus.base;

import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import org.jetbrains.annotations.Nullable;

public final class VFPDebugHudEntry implements DebugScreenEntry {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("viafabricplus", "viafabricplus");

   public void display(DebugScreenDisplayer var1, @Nullable Level var2, @Nullable LevelChunk var3, @Nullable LevelChunk var4) {
      var1.addLine("ToolVisuals Java protocols (ViaFabricPlus)");
   }

   public boolean isAllowed(boolean reducedDebugInfo) {
      return true;
   }
}
