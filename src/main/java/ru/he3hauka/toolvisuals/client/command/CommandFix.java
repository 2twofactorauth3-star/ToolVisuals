package ru.he3hauka.toolvisuals.client.command;

import java.util.Locale;
import java.util.function.Predicate;

import net.minecraft.client.Minecraft;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.COMMAND_FIX;

public final class CommandFix {
    private static final String RUSSIAN = "ёйцукенгшщзхъфывапролджэячсмитьбю";
    private static final String ENGLISH = "`qwertyuiop[]asdfghjkl;'zxcvbnm,.";
    private CommandFix() {}
    public static String outgoing(String command) {
        var connection = Minecraft.getInstance().getConnection();
        if (!ToolVisualsClient.CONFIG.active(COMMAND_FIX) || connection == null) return command;
        String result=correct(command, name -> connection.getCommands().getRoot().getChild(name) != null);
        if(ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.action(COMMAND_FIX,"checked",result.equals(command)?"unchanged":"layout_corrected");
        return result;
    }
    public static String correct(String command, Predicate<String> knownCommand) {
        int start = command.startsWith("/") ? 1 : 0, end = start;
        while (end < command.length() && !Character.isWhitespace(command.charAt(end))) end++;
        String root = command.substring(start,end);
        if (root.isEmpty() || knownCommand.test(root)) return command;
        var converted = new StringBuilder(root.length());
        boolean changed = false;
        for (int i = 0; i < root.length(); i++) {
            char original = root.charAt(i);
            int index = RUSSIAN.indexOf(Character.toLowerCase(original));
            converted.append(index < 0 ? original : ENGLISH.charAt(index));
            changed |= index >= 0;
        }
        if (!changed) return command;
        String fixed = converted.toString().toLowerCase(Locale.ROOT);
        return knownCommand.test(fixed) ? command.substring(0,start) + fixed + command.substring(end) : command;
    }
}
