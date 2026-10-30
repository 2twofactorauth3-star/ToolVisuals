package com.viaversion.viafabricplus.screen.impl.protocol;

import com.viaversion.viafabricplus.api.protocoltranslator.ProtocolTranslation;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslationImpl;
import com.viaversion.viafabricplus.screen.base.VFPTabbedScreen;
import com.viaversion.viafabricplus.screen.base.list.VFPListEntry;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.connection.ProtocolPolicy;

public abstract class AbstractProtocolSelectionScreen extends VFPTabbedScreen<ProtocolCategory> {
   private static final int SLOT_HEIGHT = 32;
   private static final int ROW_WIDTH = 260;

   protected AbstractProtocolSelectionScreen(Component title, boolean backButton) {
      super(title, backButton);
   }

   protected abstract void select(ProtocolVersion var1);

   protected abstract boolean selected(ProtocolVersion var1);

   protected abstract boolean selectable();

   @Override
   protected List<ProtocolCategory> tabs() {
      return List.of(ProtocolCategory.values());
   }

   protected Component tabTitle(ProtocolCategory tab) {
      return tab.title();
   }

   @Override
   protected int entryHeight() {
      return 32;
   }

   @Override
   protected int rowWidth(int screenWidth) {
      return Math.min(260, screenWidth - 20);
   }

   protected List<VFPListEntry> entries(ProtocolCategory tab) {
      List<VFPListEntry> entries = new ArrayList<>();
      if (tab == ProtocolCategory.MODERN) {
         entries.add(new ProtocolSlot(ProtocolTranslation.AUTO_DETECT_VERSION, this));
      }

      for (ProtocolVersion version : ProtocolPolicy.catalog(ProtocolVersion.getReversedProtocols(), ProtocolTranslationImpl.NATIVE_VERSION)) {
         if (version != ProtocolTranslation.AUTO_DETECT_VERSION && ProtocolCategory.of(version) == tab) {
            entries.add(new ProtocolSlot(version, this));
         }
      }

      return entries;
   }

   @Override
   protected List<VFPListEntry> results(String query) {
      return ProtocolPolicy.catalog(ProtocolVersion.getReversedProtocols(), ProtocolTranslationImpl.NATIVE_VERSION)
         .stream()
         .filter(version -> matches(version, query))
         .<VFPListEntry>map(version -> new ProtocolSlot(version, this))
         .toList();
   }

   protected ProtocolCategory initialTab() {
      for (ProtocolVersion version : ProtocolPolicy.catalog(ProtocolVersion.getReversedProtocols(), ProtocolTranslationImpl.NATIVE_VERSION)) {
         if (this.selected(version)) {
            return version == ProtocolTranslation.AUTO_DETECT_VERSION ? ProtocolCategory.MODERN : ProtocolCategory.of(version);
         }
      }

      return ProtocolCategory.MODERN;
   }

   private static boolean matches(ProtocolVersion version, String query) {
      if (version.getName().toLowerCase(Locale.ROOT).contains(query)) {
         return true;
      } else {
         for (String includedVersion : version.getIncludedVersions()) {
            if (includedVersion.toLowerCase(Locale.ROOT).contains(query)) {
               return true;
            }
         }

         ProtocolVersionMetadata metadata = ProtocolVersionMetadata.of(version);
         return metadata != null && metadata.title() != null && metadata.title().toLowerCase(Locale.ROOT).contains(query);
      }
   }
}
