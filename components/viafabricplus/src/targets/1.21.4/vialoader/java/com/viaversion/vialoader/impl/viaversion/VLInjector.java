package com.viaversion.vialoader.impl.viaversion;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectLinkedOpenHashSet;
import com.viaversion.viaversion.libs.gson.JsonObject;
import java.util.SortedSet;
import ru.he3hauka.toolvisuals.client.connection.ProtocolPolicy;

public class VLInjector implements ViaInjector {
   public void inject() {
   }

   public void uninject() {
   }

   public ProtocolVersion getServerProtocolVersion() {
      return this.getServerProtocolVersions().first();
   }

   public SortedSet<ProtocolVersion> getServerProtocolVersions() {
      SortedSet<ProtocolVersion> versions = new ObjectLinkedOpenHashSet();
      versions.addAll(ProtocolPolicy.catalog(ProtocolVersion.getProtocols(), ProtocolTranslator.NATIVE_VERSION));
      return versions;
   }

   public String getEncoderName() {
      return "via-codec";
   }

   public String getDecoderName() {
      return "via-codec";
   }

   public JsonObject getDump() {
      return new JsonObject();
   }
}
