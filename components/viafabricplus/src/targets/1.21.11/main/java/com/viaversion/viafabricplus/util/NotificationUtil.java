package com.viaversion.viafabricplus.util;

import com.viaversion.viafabricplus.ViaFabricPlusImpl;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public final class NotificationUtil {

    public static void warnIncompatibilityPacket(final String version, final String packet, final @Nullable String yarnMethod, final @Nullable String mojmapMethod) {
        final Logger logger = ViaFabricPlusImpl.INSTANCE.getLogger();
        logger.error("===========================================");
        logger.error("The {} packet (>= {}) could not be remapped without breaking content!", packet, version);
        logger.error("Try disabling mods one by one or using a binary search method to identify the problematic mod.");
        if (yarnMethod != null && mojmapMethod != null) {
            logger.error("Mods authors should use {} (Yarn) or {} (Mojmap) instead of sending packets directly.", yarnMethod, mojmapMethod);
        } else {
            logger.error("Mod authors should not send this packet directly.");
        }
        logger.error("Need help? Join our Discord: https://discord.gg/viaversion");
        logger.error("===========================================");
    }

}
