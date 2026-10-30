package ru.he3hauka.toolvisuals.client.connection;

import java.util.Comparator;
import java.util.List;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.network.Connection;

public final class ConnectionVersions {
    public static final class Choice {
        public final String label;
        public final ProtocolVersion protocol;
        public Choice(ProtocolVersion protocol){this.protocol=protocol;label=protocol.getName();}
    }
    private ConnectionVersions() {}
    public static List<Choice> choices(){return catalog(ProtocolVersion.getProtocols());}
    static List<Choice> catalog(List<ProtocolVersion> protocols) {
        var maximum=ProtocolVersion.getClosest("@MINECRAFT@");
        return ProtocolPolicy.catalog(protocols,maximum).stream()
                .sorted(Comparator.reverseOrder()).map(Choice::new).toList();
    }
    public static String label(){return label(null);}
    public static String label(Connection connection) {
        var bridge=ViaFabricPlus.getImpl();var connected=connection==null?null:bridge.getTargetVersion(connection);
        return (connected==null?bridge.getTargetVersion():connected).getName();
    }
    public static boolean selected(Choice choice){return ViaFabricPlus.getImpl().getTargetVersion().equals(choice.protocol);}
    public static void select(Choice choice) {
        if(!ProtocolPolicy.supported(choice.protocol,ProtocolVersion.getClosest("@MINECRAFT@")))throw new IllegalArgumentException("Unsupported Java protocol");
        var client=net.minecraft.client.Minecraft.getInstance();
        if(client.level!=null||client.getConnection()!=null)throw new IllegalStateException("Disconnect before changing protocol");
        ViaFabricPlus.getImpl().setTargetVersion(choice.protocol);
    }
}
