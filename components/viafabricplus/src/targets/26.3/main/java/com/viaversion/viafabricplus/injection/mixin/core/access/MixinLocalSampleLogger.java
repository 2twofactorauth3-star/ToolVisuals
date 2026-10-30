package com.viaversion.viafabricplus.injection.mixin.core.access;

import com.viaversion.viafabricplus.injection.access.core.ILocalSampleLogger;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslationImpl;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.util.debugchart.LocalSampleLogger;
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
      this.viaFabricPlus$forcedVersion = ProtocolPolicy.normalizeNullable(version, ProtocolTranslationImpl.NATIVE_VERSION);
   }
}
