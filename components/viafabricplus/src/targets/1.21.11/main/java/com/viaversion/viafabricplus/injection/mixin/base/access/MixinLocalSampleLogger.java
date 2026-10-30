package com.viaversion.viafabricplus.injection.mixin.base.access;

import net.minecraft.util.debugchart.LocalSampleLogger;

import com.viaversion.viafabricplus.injection.access.base.ILocalSampleLogger;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.he3hauka.toolvisuals.client.connection.ProtocolPolicy;

@Mixin(LocalSampleLogger.class)
public abstract class MixinLocalSampleLogger implements ILocalSampleLogger {
   @Unique
   private ProtocolVersion viaFabricPlus$forcedVersion;

   @Override
   public ProtocolVersion viaFabricPlus$getForcedVersion() {
      return this.viaFabricPlus$forcedVersion;
   }

   @Override
   public void viaFabricPlus$setForcedVersion(ProtocolVersion version) {
      this.viaFabricPlus$forcedVersion = ProtocolPolicy.normalizeNullable(version, ProtocolTranslator.NATIVE_VERSION);
   }
}
