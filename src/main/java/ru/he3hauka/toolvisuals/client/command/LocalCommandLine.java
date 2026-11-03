package ru.he3hauka.toolvisuals.client.command;

import java.util.List;
import java.util.Locale;

public final class LocalCommandLine {
    public static final List<String> ROOTS = List.of(".toolvisuals", ".toolvisual");
    public record Invocation(LocalCommand command, String arguments) {}

    private LocalCommandLine() {}

    public static Invocation parse(String text) {
        String[] words = text.strip().split("\\s+", 3);
        if (!root(words[0])) return null;
        return new Invocation(words.length > 1 ? LocalCommand.find(words[1]) : null,
                words.length > 2 ? words[2] : "");
    }

    private static boolean root(String text) {
        return ROOTS.stream().anyMatch(root -> root.equalsIgnoreCase(text));
    }

    public static boolean suggests(String text) {
        if (text.isEmpty() || text.charAt(0) != '.') return false;
        int end = 0;
        while (end < text.length() && !Character.isWhitespace(text.charAt(end))) end++;
        String prefix = text.substring(0, end).toLowerCase(Locale.ROOT);
        return end < text.length() ? root(prefix) : ROOTS.stream().anyMatch(root -> root.startsWith(prefix));
    }

    public static String suggestionInput(String text) {
        if (!suggests(text)) return text;
        char[] chars = text.toCharArray();
        int words = 0;
        boolean inWord = false;
        for (int i = 0; i < chars.length; i++) {
            if (Character.isWhitespace(chars[i])) { inWord = false; continue; }
            if (!inWord && ++words > 2) break;
            inWord = true;
            if (chars[i] >= 'A' && chars[i] <= 'Z') chars[i] += 'a' - 'A';
        }
        return new String(chars);
    }
}
