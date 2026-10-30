package com.viaversion.viafabricplus.screen;

import com.viaversion.viafabricplus.api.screen.Screens;
import com.viaversion.viafabricplus.features.c0_28.GridItemSelectionScreen;
import com.viaversion.viafabricplus.screen.impl.ReportIssuesScreen;
import com.viaversion.viafabricplus.screen.impl.SettingsScreen;
import com.viaversion.viafabricplus.screen.impl.ViaFabricPlusScreen;
import com.viaversion.viafabricplus.screen.impl.serverlist.BetaCraftServerListScreen;
import com.viaversion.viafabricplus.screen.impl.serverlist.ClassiCubeLoginScreen;
import com.viaversion.viafabricplus.screen.impl.serverlist.ClassiCubeMFAScreen;
import com.viaversion.viafabricplus.screen.impl.serverlist.ClassiCubeServerListScreen;
import net.minecraft.client.gui.screens.Screen;
import ru.he3hauka.toolvisuals.protocols.client.ProtocolMenus;

public final class ScreensImpl implements Screens {
   private final ViaFabricPlusScreen viaFabricPlusScreen = new ViaFabricPlusScreen();
   private final SettingsScreen settingsScreen = new SettingsScreen();
   private final ReportIssuesScreen reportIssuesScreen = new ReportIssuesScreen();
   private final ClassiCubeLoginScreen classiCubeLoginScreen = new ClassiCubeLoginScreen();
   private final ClassiCubeMFAScreen classiCubeMFAScreen = new ClassiCubeMFAScreen();
   private final ClassiCubeServerListScreen classiCubeServerListScreen = new ClassiCubeServerListScreen();
   private final BetaCraftServerListScreen betaCraftServerListScreen = new BetaCraftServerListScreen();
   private final GridItemSelectionScreen gridItemSelectionScreen = new GridItemSelectionScreen();

   public void openViaFabricPlusScreen(Screen var1) {
      ProtocolMenus.open(var1);
   }

   public void openSettingsScreen(Screen parent) {
      this.settingsScreen.open(parent);
   }

   public void openClassiCubeServerListScreen(Screen parent) {
      if (ClassiCubeServerListScreen.loggedIn()) {
         this.classiCubeServerListScreen.open(parent);
      } else {
         this.classiCubeLoginScreen.open(parent);
      }
   }

   public void openBetaCraftServerListScreen(Screen parent) {
      this.betaCraftServerListScreen.open(parent);
   }

   public ViaFabricPlusScreen viaFabricPlusScreen() {
      return this.viaFabricPlusScreen;
   }

   public SettingsScreen settingsScreen() {
      return this.settingsScreen;
   }

   public ReportIssuesScreen reportIssuesScreen() {
      return this.reportIssuesScreen;
   }

   public ClassiCubeLoginScreen classiCubeLoginScreen() {
      return this.classiCubeLoginScreen;
   }

   public ClassiCubeMFAScreen classiCubeMFAScreen() {
      return this.classiCubeMFAScreen;
   }

   public ClassiCubeServerListScreen classiCubeServerListScreen() {
      return this.classiCubeServerListScreen;
   }

   public BetaCraftServerListScreen betaCraftServerListScreen() {
      return this.betaCraftServerListScreen;
   }

   public GridItemSelectionScreen gridItemSelectionScreen() {
      return this.gridItemSelectionScreen;
   }
}
