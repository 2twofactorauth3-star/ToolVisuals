package ru.he3hauka.toolvisuals.client.localization;

import java.util.Locale;
import java.util.regex.Pattern;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class LocalizedComponents {
    private static final Pattern FORMAT = Pattern.compile("%(?:(\\d+)\\$)?([-#+ 0,(<]*)(\\d+)?(?:\\.(\\d+))?([tT])?([a-zA-Z%])");

    private LocalizedComponents() {}

    public static MutableComponent format(Locale locale, String template, Object... arguments) {
        Object[] values = new Object[arguments.length];
        for (int index = 0; index < values.length; index++)
            values[index] = arguments[index] instanceof Component component ? component.getString() : arguments[index];
        String.format(locale, template, values);
        var result = Component.empty();
        var matcher = FORMAT.matcher(template);
        int offset = 0, next = 0, previous = -1;
        while (matcher.find()) {
            result.append(template.substring(offset, matcher.start()));
            String conversion = matcher.group(6);
            if (conversion.equals("%") || conversion.equals("n")) {
                result.append(String.format(locale, matcher.group()));
            } else {
                int index = matcher.group(2).contains("<") ? previous
                        : matcher.group(1) == null ? next++ : Integer.parseInt(matcher.group(1)) - 1;
                previous = index;
                String token = matcher.group().replaceFirst("^%\\d+\\$", "%").replace("<", "");
                String text = String.format(locale, token, values[index]);
                if (arguments[index] instanceof Component component)
                    result.append(text.equals(component.getString()) ? component.copy() : Component.literal(text).setStyle(component.getStyle()));
                else result.append(text);
            }
            offset = matcher.end();
        }
        return result.append(template.substring(offset));
    }
}
