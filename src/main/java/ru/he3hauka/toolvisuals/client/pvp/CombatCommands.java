package ru.he3hauka.toolvisuals.client.pvp;

import java.util.*;

public final class CombatCommands {
    public static final List<String> DEFAULTS = List.of("hub", "lobby");
    public static final int LIMIT = 64, MAX_LENGTH = 128;
    private CombatCommands() {}
    private static String canonical(String text) {
        String value = Objects.requireNonNull(text,"Missing command").strip();
        while (value.startsWith("/")) value = value.substring(1);
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
    public static String rule(String text) {
        if (text == null || text.chars().anyMatch(Character::isISOControl)) throw new IllegalArgumentException("Invalid command");
        String value = canonical(text);
        if (value.isEmpty() || value.length() > MAX_LENGTH) throw new IllegalArgumentException("Invalid command length");
        return value;
    }
    public static List<String> normalize(Collection<String> commands) {
        var result = new LinkedHashSet<String>();
        for (String command : commands) {
            result.add(rule(command));
            if (result.size() > LIMIT) throw new IllegalArgumentException("Too many blocked commands");
        }
        return List.copyOf(result);
    }
    public static boolean blocks(String command, List<String> rules) {
        if (command == null || rules.isEmpty()) return false;
        String value = canonical(command);
        int space = value.indexOf(' '), rootEnd = space < 0 ? value.length() : space;
        int namespace = value.substring(0,rootEnd).lastIndexOf(':');
        String bare = namespace < 0 ? value : value.substring(namespace + 1);
        for (String rule : rules)
            if (matches(value,rule) || matches(bare,rule)) return true;
        return false;
    }
    private static boolean matches(String command,String rule) { return command.equals(rule) || command.startsWith(rule + " "); }
}
