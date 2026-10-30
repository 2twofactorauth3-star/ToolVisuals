package com.viaversion.viafabricplus.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import net.lenni0451.reflect.ClassLoaders;
import net.lenni0451.reflect.stream.RStream;
import org.apache.logging.log4j.Logger;

public final class ClassLoaderPriorityUtil {

    public static void loadOverridingJars(final Path path, final Logger logger) {
        try {
            final Path jars = path.resolve("jars");
            if (!Files.exists(jars)) {
                Files.createDirectory(jars);
                return;
            }

            final File[] files = jars.toFile().listFiles();
            if (files != null && files.length > 0) {
                final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
                try {
                    final ClassLoader actualLoader = RStream.of(oldLoader).fields().by("urlLoader").get();
                    Thread.currentThread().setContextClassLoader(actualLoader);

                    logger.warn("================================");
                    logger.warn("OVERRIDING JARS LOADING! THIS CAN CAUSE UNEXPECTED BEHAVIOR AND ISSUES!");
                    for (File file : files) {
                        if (file.getName().endsWith(".jar")) {
                            ClassLoaders.loadToFront(file.toURI().toURL());
                            logger.warn(" -> {}", file.getName());
                        }
                    }
                    logger.warn("================================");
                } finally {
                    Thread.currentThread().setContextClassLoader(oldLoader);
                }
            }
        } catch (Throwable e) {
            logger.error("Failed to load overriding jars", e);
        }
    }

}
