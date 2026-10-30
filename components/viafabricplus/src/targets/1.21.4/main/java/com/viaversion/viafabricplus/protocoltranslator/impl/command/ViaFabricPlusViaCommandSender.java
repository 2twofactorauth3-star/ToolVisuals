package com.viaversion.viafabricplus.protocoltranslator.impl.command;

import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import com.viaversion.viaversion.api.command.ViaCommandSender;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import java.util.UUID;

public final class ViaFabricPlusViaCommandSender implements ViaCommandSender {

    private final CommandSource source;

    public ViaFabricPlusViaCommandSender(final CommandSource source) {
        this.source = source;
    }

    @Override
    public boolean hasPermission(String s) {
        return true;
    }

    @Override
    public void sendMessage(String s) {
        ((FabricClientCommandSource) source).sendFeedback(Text.of(s.replace("/viaversion", "/viafabricplus")));
    }

    @Override
    public UUID getUUID() {
        return ((FabricClientCommandSource) source).getPlayer().getUuid();
    }

    @Override
    public String getName() {
        return ((FabricClientCommandSource) source).getPlayer().getName().getString();
    }

}
