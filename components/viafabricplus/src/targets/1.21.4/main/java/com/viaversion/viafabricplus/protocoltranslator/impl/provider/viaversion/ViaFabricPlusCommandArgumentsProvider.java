package com.viaversion.viafabricplus.protocoltranslator.impl.provider.viaversion;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.argument.SignedArgumentList;

import com.viaversion.viaversion.api.minecraft.signature.SignableCommandArgumentsProvider;
import com.viaversion.viaversion.util.Pair;
import java.util.Collections;
import java.util.List;

public final class ViaFabricPlusCommandArgumentsProvider extends SignableCommandArgumentsProvider {

    @Override
    public List<Pair<String, String>> getSignableArguments(String command) {
        final ClientPlayNetworkHandler network = MinecraftClient.getInstance().getNetworkHandler();

        if (network != null) {
            return SignedArgumentList.of(
                            network.getCommandDispatcher().parse(command, network.getCommandSource())).
                    arguments().stream().
                    map(function -> new Pair<>(function.getNodeName(), function.value())).
                    toList();
        } else {
            return Collections.emptyList();
        }
    }

}
