package ru.he3hauka.toolvisuals.client.replay.io;

import java.util.Objects;
import java.util.UUID;

public final class ReplayHeader {
    public final UUID recording, player;
    public final String playerName, gameVersion;
    public final int protocol;
    public final long createdMillis;

    public ReplayHeader(UUID recording, UUID player, String playerName, String gameVersion, int protocol, long createdMillis) {
        if (playerName == null || !playerName.matches("[A-Za-z0-9_]{1,16}")) throw new IllegalArgumentException("Invalid recorded player name");
        if (gameVersion == null || gameVersion.isBlank() || gameVersion.length() > 32) throw new IllegalArgumentException("Invalid recorded game version");
        if (protocol < 0) throw new IllegalArgumentException("Invalid recorded protocol");
        this.recording = Objects.requireNonNull(recording);
        this.player = Objects.requireNonNull(player);
        this.playerName = playerName;
        this.gameVersion = gameVersion;
        this.protocol = protocol;
        this.createdMillis = createdMillis;
    }
}
