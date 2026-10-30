package ru.he3hauka.toolvisuals.client.social.badge;

import java.util.Arrays;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public final class BadgeLabels {
    public static final Component ICON = Component.literal("\ue000").withStyle(style -> style
            .withFont(new FontDescription.Resource(Identifier.parse("toolvisuals:badge"))).withColor(0xb59aff));

    public enum Position {
        RIGHT("Badge right"), LEFT("Badge left"), ABOVE("Badge above");

        private static final Position[] ALL = values();
        public final String choice;

        Position(String choice) { this.choice = choice; }

        public static Position at(int index) { return ALL[Math.clamp(index, 0, ALL.length - 1)]; }
        public static String[] choices() { return Arrays.stream(ALL).map(position -> position.choice).toArray(String[]::new); }
    }

    private BadgeLabels() {}

    public static Component apply(Component name, boolean visible, Position position, BadgeRenderState state) {
        state.toolvisuals$badgeAbove(visible && name != null && position == Position.ABOVE);
        if (!visible || name == null) return name;
        return switch (position) {
            case RIGHT -> Component.empty().append(name).append(" ").append(ICON);
            case LEFT -> Component.empty().append(ICON).append(" ").append(name);
            case ABOVE -> name;
        };
    }
}
