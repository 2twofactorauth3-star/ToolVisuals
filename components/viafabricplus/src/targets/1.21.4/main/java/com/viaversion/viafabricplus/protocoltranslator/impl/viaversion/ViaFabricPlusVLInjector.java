package com.viaversion.viafabricplus.protocoltranslator.impl.viaversion;

import com.viaversion.vialoader.impl.viaversion.VLInjector;
import com.viaversion.vialoader.netty.VLLegacyPipeline;

public final class ViaFabricPlusVLInjector extends VLInjector {

    @Override
    public String getEncoderName() {
        return VLLegacyPipeline.VIA_ENCODER_NAME;
    }

    @Override
    public String getDecoderName() {
        return VLLegacyPipeline.VIA_DECODER_NAME;
    }

}
