package com.viaversion.vialoader.util;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ru.he3hauka.toolvisuals.client.connection.ProtocolPolicy;

public class ProtocolVersionList {
   public static List<ProtocolVersion> getProtocolsNewToOld() {
      List<ProtocolVersion> protocolVersions = new ArrayList<>(ProtocolPolicy.catalog(ProtocolVersion.getProtocols(), ProtocolTranslator.NATIVE_VERSION));
      Collections.reverse(protocolVersions);
      return Collections.unmodifiableList(protocolVersions);
   }
}
