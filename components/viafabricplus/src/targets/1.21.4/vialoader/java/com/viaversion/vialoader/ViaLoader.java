package com.viaversion.vialoader;

import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.commands.ViaCommandHandler;
import com.viaversion.viaversion.protocol.ProtocolManagerImpl;
import com.viaversion.vialoader.impl.platform.ViaVersionPlatformImpl;
import com.viaversion.vialoader.impl.viaversion.VLCommandHandler;
import com.viaversion.vialoader.impl.viaversion.VLInjector;
import com.viaversion.vialoader.impl.viaversion.VLLoader;
import com.viaversion.vialoader.util.JLoggerToSLF4J;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViaLoader {

    public static final String VERSION = "${version}";

    private static final Logger LOGGER = new JLoggerToSLF4J(LoggerFactory.getLogger("ViaLoader"));

    @SuppressWarnings("ReassignedVariable")
    public static void init(ViaPlatform<?> platform, ViaPlatformLoader loader, ViaInjector injector, ViaCommandHandler commandHandler, final Supplier<?>... platformSuppliers) {
        if (platform == null) platform = new ViaVersionPlatformImpl(null);
        if (loader == null) loader = new VLLoader();
        if (injector == null) injector = new VLInjector();
        if (commandHandler == null) commandHandler = new VLCommandHandler();

        Via.init(ViaManagerImpl.builder()
                .platform(platform)
                .loader(loader)
                .injector(injector)
                .commandHandler(commandHandler)
                .build());

        Via.getConfig().reload();

        if (platformSuppliers != null) {
            Via.getManager().addEnableListener(() -> {
                for (Supplier<?> additionalPlatformSupplier : platformSuppliers) {
                    try {
                        additionalPlatformSupplier.get();
                    } catch (Throwable e) {
                        LOGGER.log(Level.SEVERE, "Platform failed to load", e);
                    }
                }
            });
        }

        final ViaManagerImpl viaManager = (ViaManagerImpl) Via.getManager();
        viaManager.init();
        viaManager.onServerLoaded();

        Via.getManager().getProtocolManager().setMaxProtocolPathSize(Integer.MAX_VALUE);
        Via.getManager().getProtocolManager().setMaxPathDeltaIncrease(-1);
        ((ProtocolManagerImpl) Via.getManager().getProtocolManager()).refreshVersions();
    }

}
