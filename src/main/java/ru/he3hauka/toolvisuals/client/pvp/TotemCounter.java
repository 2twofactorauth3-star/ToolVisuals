package ru.he3hauka.toolvisuals.client.pvp;

import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.player.Player;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.player.ClientActions;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.TOTEM_POP_COUNTER;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class TotemCounter {
    private static final Map<UUID,Integer> COUNTS=new LinkedHashMap<>();
    private static Object connection;
    private TotemCounter() {}
    public static void tick(Minecraft client){if(connection!=client.getConnection()){connection=client.getConnection();clear();}}
    public static void clear(){COUNTS.clear();}
    public static int count(UUID id){return COUNTS.getOrDefault(id,0);}
    public static void event(ClientboundEntityEventPacket packet) {
        var client=Minecraft.getInstance();tick(client);
        if(client.level==null||client.player==null||!ToolVisualsClient.CONFIG.active(TOTEM_POP_COUNTER))return;
        if(!(packet.getEntity(client.level) instanceof Player player))return;
        if(packet.getEventId()==3&&ToolVisualsClient.CONFIG.flag(TOTEM_RESET_DEATH)){COUNTS.remove(player.getUUID());return;}
        if(packet.getEventId()!=35||!ToolVisualsClient.CONFIG.flag(player==client.player?TOTEM_SELF:TOTEM_OTHERS))return;
        if(!COUNTS.containsKey(player.getUUID())&&COUNTS.size()>=1024)COUNTS.remove(COUNTS.keySet().iterator().next());
        int count=COUNTS.merge(player.getUUID(),1,Integer::sum);
        if(ToolVisualsClient.CONFIG.flag(TOTEM_NOTIFY))ClientActions.notice("totem.popped",player.getName(),count);
    }
}
