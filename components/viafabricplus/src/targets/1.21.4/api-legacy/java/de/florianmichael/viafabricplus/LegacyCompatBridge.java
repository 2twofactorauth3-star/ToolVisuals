package de.florianmichael.viafabricplus;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.base.event.EventFactoryImpl;

import java.util.function.Function;

public class LegacyCompatBridge {

    private static boolean warned = false;

    public static <T> Event<T> createArrayBacked(Class<? super T> type, Function<T[], T> invokerFactory) {
        warn();
        return EventFactoryImpl.createArrayBacked(type, invokerFactory);
    }

    public static void warn() {
        if (!warned) {
            warned = true;
            ViaFabricPlus.INSTANCE.getLogger().error("===========================================");
            ViaFabricPlus.INSTANCE.getLogger().error("A mod is using deprecated ViaFabricPlus internals which will be removed in the future.");
            ViaFabricPlus.INSTANCE.getLogger().error("Please contact the mod author to update their code to use the general API point added in 4.0.0.");
            ViaFabricPlus.INSTANCE.getLogger().error("The error will point to the mod calling the deprecated API:", new Exception());
            ViaFabricPlus.INSTANCE.getLogger().error("===========================================");
        }
    }

}
