package ru.he3hauka.toolvisuals.client.replay.capture;

import java.util.Set;

public final class ReplayPacketPolicy {
    private static final Set<String> CONFIGURATION = Set.of("registry_data", "update_tags", "update_enabled_features",
            "select_known_packs", "finish_configuration", "reset_chat");
    private static final Set<String> EXCLUDED = Set.of("bundle_delimiter", "custom_payload", "transfer", "disconnect",
            "cookie_request", "store_cookie", "resource_pack_push", "resource_pack_pop", "server_links", "show_dialog",
            "clear_dialog", "code_of_conduct", "keep_alive", "ping", "sound", "sound_entity", "stop_sound",
            "open_screen", "open_book", "open_sign_editor", "commands", "command_suggestions", "server_data");
    private static final Set<String> CHAT = Set.of("player_chat", "disguised_chat", "system_chat", "delete_chat", "player_chat_header");

    private ReplayPacketPolicy() {}

    public static boolean allow(boolean configuration, String namespace, String path, boolean chat) {
        if (!"minecraft".equals(namespace) || path == null) return false;
        if (configuration) return CONFIGURATION.contains(path);
        return !EXCLUDED.contains(path) && (chat || !CHAT.contains(path));
    }
}
