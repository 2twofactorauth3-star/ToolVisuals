package com.viaversion.viafabricplus.injection.mixin.base.access;

import net.minecraft.util.profiler.MultiValueDebugSampleLogImpl;

import com.viaversion.viafabricplus.injection.access.base.IMultiValueDebugSampleLogImpl;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.he3hauka.toolvisuals.client.connection.ProtocolPolicy;

@Mixin(MultiValueDebugSampleLogImpl.class)
public abstract class MixinMultiValueDebugSampleLogImpl implements IMultiValueDebugSampleLogImpl {
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
