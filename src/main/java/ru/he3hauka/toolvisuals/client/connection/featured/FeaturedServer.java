package ru.he3hauka.toolvisuals.client.connection.featured;

import java.util.List;
import java.util.Locale;

import net.minecraft.client.multiplayer.ServerData;

import ru.he3hauka.toolvisuals.client.localization.UiI18n;

public final class FeaturedServer {
    public static final String ADDRESS = "mc.toolrise.space";
    private FeaturedServer() {}

    public static boolean canSwap(int first, int second) { return first > 0 && second > 0; }

    public static boolean matches(ServerData data) {
        if (data == null || data.ip == null) return false;
        String address = data.ip.trim().toLowerCase(Locale.ROOT);
        return address.equals(ADDRESS) || address.equals(ADDRESS + ":25565")
                || address.equals(ADDRESS + ".") || address.equals(ADDRESS + ".:25565");
    }

     
    public static boolean ensure(List<ServerData> servers, List<ServerData> hidden) {
        boolean changed = false;
        ServerData featured = null;
        for (var data : servers) if (matches(data)) { featured = data; break; }
        if (featured == null) for (var data : hidden) if (matches(data)) { featured = data; break; }
        if (featured == null) featured = new ServerData("", ADDRESS, ServerData.Type.OTHER);
        String name = UiI18n.tr("toolvisuals.server.best").getString();
        if (!featured.name.equals(name) || !featured.ip.equals(ADDRESS)) changed = true;
        featured.name = name; featured.ip = ADDRESS;
        for (int i = servers.size() - 1; i >= 0; i--) {
            if (matches(servers.get(i)) && (i != 0 || servers.get(i) != featured)) {
                servers.remove(i); changed = true;
            }
        }
        if (servers.isEmpty() || servers.getFirst() != featured) { servers.addFirst(featured); changed = true; }
        return hidden.removeIf(FeaturedServer::matches) || changed;
    }
}
