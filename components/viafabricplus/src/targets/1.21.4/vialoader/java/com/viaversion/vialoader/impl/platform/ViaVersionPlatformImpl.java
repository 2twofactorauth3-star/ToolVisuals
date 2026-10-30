package com.viaversion.vialoader.impl.platform;

import com.viaversion.vialoader.ViaLoader;
import com.viaversion.vialoader.impl.viaversion.VLApiBase;
import com.viaversion.vialoader.impl.viaversion.VLViaConfig;
import com.viaversion.vialoader.util.JLoggerToSLF4J;
import com.viaversion.vialoader.util.PacketTypeUtil;
import com.viaversion.vialoader.util.VLTask;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.configuration.ViaVersionConfig;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.configuration.AbstractViaConfig;
import com.viaversion.viaversion.protocols.base.InitialBaseProtocol;
import com.viaversion.viaversion.util.VersionInfo;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ViaVersionPlatformImpl implements ViaPlatform<UserConnection> {

    private static final Logger LOGGER = new JLoggerToSLF4J(LoggerFactory.getLogger("ViaVersion"));

    private final File dataFolder;
    private final AbstractViaConfig config;
    private final ViaAPI<UserConnection> api;

    public ViaVersionPlatformImpl(final File rootFolder) {
        this.dataFolder = new File(rootFolder, "ViaLoader");
        this.config = this.createConfig();
        this.api = this.createApi();
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public String getPlatformName() {
        return "ViaLoader";
    }

    @Override
    public String getPlatformVersion() {
        return ViaLoader.VERSION;
    }

    @Override
    public String getPluginVersion() {
        return VersionInfo.getVersion();
    }

    @Override
    public VLTask runAsync(Runnable runnable) {
        return new VLTask(Via.getManager().getScheduler().execute(runnable));
    }

    @Override
    public VLTask runRepeatingAsync(Runnable runnable, long period) {
        return new VLTask(Via.getManager().getScheduler().scheduleRepeating(runnable, 0, period * 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public VLTask runSync(Runnable runnable) {
        return this.runAsync(runnable);
    }

    @Override
    public VLTask runSync(Runnable runnable, long delay) {
        return new VLTask(Via.getManager().getScheduler().schedule(runnable, delay * 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public VLTask runRepeatingSync(Runnable runnable, long period) {
        return this.runRepeatingAsync(runnable, period);
    }

    @Override
    public void sendCustomPayload(UserConnection connection, String channel, byte[] message) {
        final ServerboundPacketType packetType = PacketTypeUtil.getServerboundPacketType("CUSTOM_PAYLOAD", connection);
        if (packetType == null) {
            throw new IllegalStateException("Cannot send custom payload if client and server are on the same version or if the server version does not support it");
        }

        final PacketWrapper customPayload = PacketWrapper.create(packetType, connection);
        customPayload.write(Types.STRING, channel);
        customPayload.write(Types.REMAINING_BYTES, message);
        customPayload.scheduleSendToServer(InitialBaseProtocol.class);
    }

    @Override
    public boolean hasPlugin(String name) {
        return false;
    }

    @Override
    public boolean couldBeReloading() {
        return false;
    }

    @Override
    public boolean isProxy() {
        return true;
    }

    @Override
    public ViaAPI<UserConnection> getApi() {
        return this.api;
    }

    @Override
    public ViaVersionConfig getConf() {
        return this.config;
    }

    @Override
    public File getDataFolder() {
        return this.dataFolder;
    }

    protected AbstractViaConfig createConfig() {
        return new VLViaConfig(new File(this.dataFolder, "viaversion.yml"), this.getLogger());
    }

    protected ViaAPI<UserConnection> createApi() {
        return new VLApiBase();
    }

}
