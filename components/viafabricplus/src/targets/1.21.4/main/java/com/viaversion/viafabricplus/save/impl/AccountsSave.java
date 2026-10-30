package com.viaversion.viafabricplus.save.impl;

import com.google.gson.JsonObject;
import com.viaversion.viafabricplus.save.AbstractSave;
import de.florianmichael.classic4j.model.classicube.account.CCAccount;

public final class AccountsSave extends AbstractSave {
   private CCAccount classicubeAccount;
   private JsonObject toolvisuals$preserved;

   public AccountsSave() {
      super("accounts");
   }

   @Override
   public void write(JsonObject var1) {
      if (this.toolvisuals$preserved != null) {
         var1.asMap().putAll(this.toolvisuals$preserved.deepCopy().asMap());
      }
   }

   @Override
   public void read(JsonObject var1) {
      this.toolvisuals$preserved = var1.deepCopy();
   }

   public CCAccount getClassicubeAccount() {
      return this.classicubeAccount;
   }

   public void setClassicubeAccount(CCAccount classicubeAccount) {
      this.classicubeAccount = classicubeAccount;
   }

   @FunctionalInterface
   interface AccountConsumer {
      void accept(JsonObject var1) throws Exception;
   }
}
