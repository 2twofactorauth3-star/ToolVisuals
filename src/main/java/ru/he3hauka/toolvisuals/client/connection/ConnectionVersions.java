package ru.he3hauka.toolvisuals.client.connection;

import java.util.Comparator;
import java.util.List;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionType;
import net.minecraft.network.Connection;

public final class ConnectionVersions {
    public static final class Choice {
        public final String label;
        public final ProtocolVersion protocol;
        public Choice(ProtocolVersion protocol) {
            this.protocol = protocol;
            label = protocol.getName();
        }
    }
    private ConnectionVersions() {}
    public static List<Choice> choices() {
        return catalog(ProtocolVersion.getProtocols());
    }
    static List<Choice> catalog(List<ProtocolVersion> protocols){
        return protocols.stream().filter(p->p!=ProtocolVersion.unknown&&!p.isSnapshot()&&p.getVersionType()!=VersionType.SPECIAL&&p.olderThanOrEqualTo(ProtocolVersion.v26_3))
                .distinct().sorted(Comparator.reverseOrder()).map(Choice::new).toList();
    }
    public static String label() {
        return label(null);
    }
    public static String label(Connection connection) {
        var translation=ViaFabricPlus.api().protocolTranslation();
        var connected=connection==null?null:translation.targetVersion(connection);
        return (connected==null?translation.targetVersion():connected).getName();
    }
    public static boolean selected(Choice choice) { return ViaFabricPlus.api().targetVersion().equals(choice.protocol); }
    public static void select(Choice choice) {
        var client = net.minecraft.client.Minecraft.getInstance();
        if (client.level != null || client.getConnection() != null) throw new IllegalStateException("Disconnect before changing protocol");
        ViaFabricPlus.api().setTargetVersion(choice.protocol);
    }
}
