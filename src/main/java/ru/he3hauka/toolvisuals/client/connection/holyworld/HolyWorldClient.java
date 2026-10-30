package ru.he3hauka.toolvisuals.client.connection.holyworld;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.chat.message.ChatMessages;
import ru.he3hauka.toolvisuals.client.connection.holyworld.network.FeatureControlPayload;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

public final class HolyWorldClient {
    private static final HolyWorldSession SESSION = new HolyWorldSession(ToolVisualsClient.CONFIG.serverPolicy);
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("ToolVisuals/HolyWorld");
    private static long appliedRevision;
    private static boolean announced;

    private HolyWorldClient() {}

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(FeatureControlPayload.TYPE, FeatureControlPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(FeatureControlPayload.TYPE, FeatureControlPayload.CODEC);
        ClientConfigurationConnectionEvents.INIT.register((handler, client) -> begin(handler, client));
        ClientConfigurationConnectionEvents.DISCONNECT.register((handler, client) -> reset());
        ClientPlayConnectionEvents.INIT.register((handler, client) -> begin(handler, client));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> tick(client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
        ClientPlayNetworking.registerGlobalReceiver(FeatureControlPayload.TYPE, (payload, context) -> {
            Object connection = context.player().connection;
            receive(connection, payload, context.client());
        });
        ClientTickEvents.START_CLIENT_TICK.register(HolyWorldClient::tick);
    }

    private static void begin(Object connection, Minecraft client) {
        SESSION.begin(connection, address(client));
        announced = false;
    }

    private static void reset() {
        SESSION.reset();
        announced = false;
    }

    public static String address(Minecraft client) {
        var server = client.getCurrentServer();
        return server == null ? "" : server.ip;
    }

    public static boolean captureAllowed(Minecraft client) {
        return !HolyWorldHosts.matches(address(client));
    }
    public static boolean protectedServer() { return SESSION.protectedServer(); }
    public static void status() {
        String status = SESSION.confirmed() ? "confirmed" : SESSION.protectedServer() ? "fallback" : "unconfirmed";
        ChatMessages.notice("holyworld." + status, ToolVisualsClient.CONFIG.serverPolicy.blocked().size());
    }

    private static void tick(Minecraft client) {
        if (ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active()) return;
        if (client.getConnection() == null || client.player == null || client.level == null) return;
        SESSION.advertise(ClientPlayNetworking.canSend(FeatureControlPayload.TYPE));
        byte[] request = SESSION.request(System.nanoTime());
        if (request != null) {
            try {
                ClientPlayNetworking.send(new FeatureControlPayload(request));
            } catch (RuntimeException error) {
                ClientErrors.report("Send HolyWorld feature-control request", error);
            }
        }
        apply(client);
    }

    private static void receive(Object connection, FeatureControlPayload payload, Minecraft client) {
        long now = System.nanoTime();
        if (client.getConnection() != connection || !SESSION.admit(connection, payload.bytes().length, now)) return;
        try {
            var response = HolyWorldProtocol.response(payload.bytes());
            if (!response.ok() && SESSION.pending(connection, response.id())) {
                LOGGER.warn("HolyWorld feature-control request rejected: {}", response.error().replaceAll("[\\p{Cntrl}]", " "));
                return;
            }
            if (!SESSION.accept(connection, response, now)) return;
            LOGGER.info("HolyWorld feature-control response verified; {} features restricted for this connection",
                    ToolVisualsClient.CONFIG.serverPolicy.blocked().size());
            apply(client);
        } catch (RuntimeException error) {
            ClientErrors.report("Read HolyWorld feature-control response", error);
        }
    }

    private static void apply(Minecraft client) {
        var policy = ToolVisualsClient.CONFIG.serverPolicy;
        if (policy.revision() != appliedRevision) {
            appliedRevision = policy.revision();
            if (ru.he3hauka.toolvisuals.client.connection.policy.FeatureScreens.blocked(client.gui.screen())
                    || client.gui.screen() instanceof net.minecraft.client.gui.screens.options.controls.KeyBindsScreen)
                client.gui.setScreen(null);
        }
        if (!announced && SESSION.protectedServer()) {
            announced = true;
            ChatMessages.notice("holyworld.protected");
        }
    }
}
