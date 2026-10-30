package com.viaversion.vialoader.commands;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.connection.UserConnection;

import java.util.UUID;

public class UserCommandSender implements ViaCommandSender {

    private final UserConnection connection;

    public UserCommandSender(final UserConnection connection) {
        this.connection = connection;
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public void sendMessage(String msg) {
        Via.getPlatform().sendMessage(connection, msg);
    }

    @Override
    public UUID getUUID() {
        return this.connection.getProtocolInfo().getUuid();
    }

    @Override
    public String getName() {
        return this.connection.getProtocolInfo().getUsername();
    }

}
