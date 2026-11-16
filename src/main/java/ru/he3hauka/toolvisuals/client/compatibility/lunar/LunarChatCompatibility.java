package ru.he3hauka.toolvisuals.client.compatibility.lunar;

import java.util.Set;


public final class LunarChatCompatibility {
    public static final String VIA_VERSION="5.1.1";
    public static final String VIA_CONFIG="viafabricplus.mixins.json";
    public static final String LUNAR_CONFIG="mixins.modern_lunar_combined.json";
    public static final String ORIGINAL_PLUGIN="com.viaversion.viafabricplus.injection.ViaFabricPlusMixinPlugin";
    public static final String COMPATIBILITY_PLUGIN="ru.he3hauka.toolvisuals.client.compatibility.lunar.ViaFabricPlusPlugin";
    public static final String CHAT_TARGET="net.minecraft.network.protocol.game.ServerboundChatPacket";
    public static final String CHAT_MIXIN="com.viaversion.viafabricplus.injection.mixin.features.v1_9_3.network.MixinServerboundChatPacket";
    private static final Set<String> VERSIONS=Set.of("5.1.1","4.4.15","4.0.5-BACKPORT");

    private LunarChatCompatibility() {}

    public static boolean chatCodec(String target,String mixin) {
        return CHAT_TARGET.equals(target)&&CHAT_MIXIN.equals(mixin);
    }
    public static String upstreamVersion(String version){return version.replaceFirst("\\+toolvisuals\\.[0-9]+$", "");}
    public static boolean reviewedVersion(String version){return VERSIONS.contains(upstreamVersion(version));}

    public static boolean lunarConfig(Iterable<String> names) {
        for(String name:names)if(LUNAR_CONFIG.equals(name))return true;
        return false;
    }
}
