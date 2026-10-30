package ru.he3hauka.toolvisuals.client.friends;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import ru.he3hauka.toolvisuals.client.localization.UiLanguage;

final class FriendLabelCache {
    private static final int LIMIT = 512;
    private final LinkedHashMap<String, Entry> entries = new LinkedHashMap<>();
    private FriendStore store;
    private long revision;

    Entry get(FriendStore source, String profile) {
        if (source != store || source.revision() != revision) {
            entries.clear();
            store = source;
            revision = source.revision();
        }
        var entry = entries.get(profile);
        if (entry == null) {
            if (entries.size() >= LIMIT) entries.remove(entries.keySet().iterator().next());
            entry = new Entry(source.get(profile));
            entries.put(profile, entry);
        }
        return entry;
    }

    private record Fragment(Style style, String text) {}

    static final class Entry implements FormattedText.StyledContentConsumer<Boolean> {
        private static final Optional<Boolean> STOP = Optional.of(Boolean.TRUE);
        final FriendStore.Friend friend;
        private final ArrayList<Fragment> fragments = new ArrayList<>();
        private boolean capture, valid;
        private int index, characters;
        Component name, badge;
        int nameColor = -1, badgeColor = -1;
        UiLanguage badgeLanguage;

        Entry(FriendStore.Friend friend) { this.friend = friend; }

        boolean matches(Component source) {
            capture = false;
            valid = true;
            index = 0;
            source.visit(this, Style.EMPTY);
            return valid && index == fragments.size() && nameColor >= 0;
        }

        boolean capture(Component source) {
            fragments.clear();
            capture = valid = true;
            characters = index = 0;
            source.visit(this, Style.EMPTY);
            capture = false;
            if (!valid) fragments.clear();
            return valid;
        }

        @Override public Optional<Boolean> accept(Style style, String text) {
            if (capture) {
                characters += text.length();
                if (fragments.size() >= 128 || characters > 2048) { valid = false; return STOP; }
                fragments.add(new Fragment(style, text));
            } else {
                if (index >= fragments.size()) { valid = false; return STOP; }
                var fragment = fragments.get(index++);
                if (!fragment.text.equals(text) || !fragment.style.equals(style)) { valid = false; return STOP; }
            }
            return Optional.empty();
        }
    }
}
