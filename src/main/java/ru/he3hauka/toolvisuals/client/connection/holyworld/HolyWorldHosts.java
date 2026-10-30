package ru.he3hauka.toolvisuals.client.connection.holyworld;

import java.net.IDN;
import java.util.Locale;

public final class HolyWorldHosts {
    private HolyWorldHosts() {}

    public static boolean matches(String address) {
        if (address == null) return false;
        String host = address.strip();
        int colon = host.indexOf(':');
        if (colon >= 0) host = host.substring(0, colon);
        try {
            host = IDN.toASCII(host, IDN.USE_STD3_ASCII_RULES).toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException error) {
            return false;
        }
        while (host.endsWith(".")) host = host.substring(0, host.length() - 1);
        return host.equals("holyworld.me") || host.endsWith(".holyworld.me")
                || host.equals("holyworld.ru") || host.endsWith(".holyworld.ru");
    }
}
