package ru.he3hauka.toolvisuals.client.replay.playback;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.server.ServerLinks;
import net.minecraft.world.flag.FeatureFlags;
import ru.he3hauka.toolvisuals.client.replay.capture.ReplayPacketPolicy;
import ru.he3hauka.toolvisuals.client.replay.io.*;
import ru.he3hauka.toolvisuals.mixin.replay.ReplayGuiAccessor;
import ru.he3hauka.toolvisuals.mixin.replay.ReplayParticleAccessor;

public final class ReplayScene implements AutoCloseable {
    public final ReplayHeader header;
    private ReplaySceneState state;
    private ReplayConnection connection;
    private net.minecraft.client.player.RemotePlayer actor, camera;
    private long position;
    private final net.minecraft.client.DeltaTracker delta=new net.minecraft.client.DeltaTracker(){
        @Override public float getGameTimeDeltaTicks(){return 0;}
        @Override public float getGameTimeDeltaPartialTick(boolean ignored){return position%50/50f;}
        @Override public float getRealtimeDeltaTicks(){return 1;}
    };
    private boolean closed;

    public ReplayScene(ReplayHeader header) {
        this.header = header;
        var client = Minecraft.getInstance();
        var game = new GameRenderer(client, new FirstPersonHandsAndItemsRenderer(client), client.getModelManager(), client.getItemModelResolver());
        LevelRenderer renderer = null;
        try {
            renderer = new LevelRenderer(client.getEntityRenderDispatcher(), client.getBlockEntityRenderDispatcher(), client.getModelManager(),
                    client.getTextureManager(), client.getAtlasManager(), client.getShaderManager(), game, client.getWindow().getWidth(), client.getWindow().getHeight());
            var extractor = new LevelExtractor(client, game.gameRenderState().levelRenderState, renderer);
            var particles = new ParticleEngine(null, ((ReplayParticleAccessor)client.particleEngine).toolvisuals$replayResources());
            var gui = new Gui(client, new Hud(client), game.gameRenderState().guiRenderState);
            state = new ReplaySceneState(null, null, null, extractor, renderer, particles, game, gui, null, null);
            ReplayScope.run(this, () -> {
                game.resize(client.getWindow().getWidth(), client.getWindow().getHeight());
                extractor.onResourceManagerReload(client.getResourceManager());
                connection = new ReplayConnection();
                var telemetry = new WorldSessionTelemetryManager(TelemetryEventSender.DISABLED, false, Duration.ZERO, null, header.player);
                var cookie = new CommonListenerCookie(new LevelLoadTracker(0), new GameProfile(header.player, header.playerName), telemetry,
                        RegistryAccess.EMPTY, FeatureFlags.DEFAULT_FLAGS, "ToolVisuals Replay", null, null, new HashMap<>(), null,
                        Map.of(), ServerLinks.EMPTY, new HashMap<>(), true);
                connection.setupOutboundProtocol(ConfigurationProtocols.SERVERBOUND_TEMPLATE.bind(FriendlyByteBuf::new));
                connection.setupInboundProtocol(ConfigurationProtocols.CLIENTBOUND_TEMPLATE.bind(FriendlyByteBuf::new),
                        new ClientConfigurationPacketListenerImpl(client, connection, cookie));
            });
        } catch (RuntimeException | Error error) {
            if (connection != null) connection.close();
            if (renderer != null) renderer.close();
            game.close(); throw error;
        }
    }

    ReplaySceneState state() { return state; }
    void capture(Minecraft client) { state = new ReplaySceneState(client); }
    public boolean ready() { return !closed && state.level != null && state.player != null; }
    public void position(long millis){position=millis;}
    public net.minecraft.client.DeltaTracker delta(){return delta;}

    private net.minecraft.client.player.RemotePlayer actor(int id) {
        var client = Minecraft.getInstance();
        if (actor != null && actor.level() == client.level && !actor.isRemoved() && actor.getId() == id) return actor;
        client.level.removeEntity(id, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        ((ru.he3hauka.toolvisuals.mixin.entity.EntityRemovalInvoker)client.player).toolvisuals$restoreRemoval();
        client.player.setId(Integer.MIN_VALUE + 1);
        actor = new net.minecraft.client.player.RemotePlayer(client.level, new GameProfile(header.player, header.playerName)); actor.setId(id);
        client.level.addEntity(actor);
        camera = new net.minecraft.client.player.RemotePlayer(client.level, new GameProfile(java.util.UUID.randomUUID(), "ReplayCamera"));
        camera.setPos(client.player.getX() + 4, client.player.getY() + 2, client.player.getZ() + 4); camera.setOldPosAndRot();
        client.setCameraEntity(camera);
        return actor;
    }

    public void move(double forward, double sideways, double vertical, float yawDelta, float pitchDelta) {
        if (camera == null) return;
        ReplayScope.run(this, () -> {
            camera.setYRot(camera.getYRot() + yawDelta); camera.setXRot(Math.clamp(camera.getXRot() + pitchDelta, -90, 90));
            double yaw = Math.toRadians(camera.getYRot());
            camera.setPos(camera.getX() - Math.sin(yaw) * forward + Math.cos(yaw) * sideways,
                    camera.getY() + vertical, camera.getZ() + Math.cos(yaw) * forward + Math.sin(yaw) * sideways);
            camera.setOldPosAndRot();
        });
    }

    public void follow() {
        if (camera == null || actor == null) return;
        ReplayScope.run(this, () -> {
            camera.setPos(actor.getX() + 4, actor.getY() + 2, actor.getZ() + 4);
            camera.setYRot(135); camera.setXRot(15); camera.setOldPosAndRot();
        });
    }

    public void tick() {
        if (!ready()) return;
        ReplayScope.run(this, () -> {
            state.level.tick(() -> true); state.level.tickEntities(); state.particles.tick(); state.game.tick();
            state.level.getChunkSource().getLightEngine().runLightUpdates();
            if(connection.getPacketListener() instanceof net.minecraft.network.TickablePacketListener listener)listener.tick();
        });
    }

    public void screen(Screen screen) { ((ReplayGuiAccessor)state.gui).toolvisuals$replayScreen(screen); }

    public void level(ClientLevel level) {
        var client = Minecraft.getInstance(); client.level = level;
        client.levelExtractor.setLevel(level); client.particleEngine.setLevel(level); client.gameRenderer.setLevel(level);
    }

    public void clearLevel(){
        var client=Minecraft.getInstance();
        if(client.getConnection()!=null)client.getConnection().clearLevel();
        client.player=null;client.gameMode=null;actor=null;camera=null;level(null);client.gameRenderer.resetData();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void apply(ReplayFrame frame) throws IOException {
        if (closed || frame.kind == ReplayFrame.Kind.END) return;
        if (frame.kind == ReplayFrame.Kind.SELF) {
            if (ready()) try {
                var previousCamera=camera;
                ReplayScope.run(this, () -> ru.he3hauka.toolvisuals.client.replay.capture.ReplaySelf.apply(frame,
                        state.level.registryAccess(), state.level.dimension().identifier().toString(), this::actor));
                if(previousCamera!=camera)follow();
            } catch (RuntimeException error) { throw new IOException("Cannot apply recorded player pose", error); }
            return;
        }
        final IOException[] failure = new IOException[1];
        ReplayScope.run(this, () -> {
            var expected = frame.kind == ReplayFrame.Kind.CONFIGURATION ? ConnectionProtocol.CONFIGURATION : ConnectionProtocol.PLAY;
            if (connection.inbound().id() != expected) { failure[0] = new IOException("Replay protocol phase is inconsistent"); return; }
            var bytes = Unpooled.wrappedBuffer(frame.payload.duplicate().clear());
            try {
                Packet packet = (Packet)connection.inbound().codec().decode(bytes);
                if (bytes.isReadable()) throw new IllegalArgumentException("Replay packet has trailing bytes");
                var id = packet.type().id();
                if(packet instanceof net.minecraft.network.protocol.game.ClientboundGameEventPacket event
                        &&event.getEvent()==net.minecraft.network.protocol.game.ClientboundGameEventPacket.WIN_GAME)return;
                if(packet instanceof net.minecraft.network.protocol.game.ClientboundLoginPacket login&&(login.chunkRadius()<0||login.chunkRadius()>64)
                        ||packet instanceof net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket radius&&(radius.getRadius()<0||radius.getRadius()>64))
                    throw new IllegalArgumentException("Recorded chunk radius exceeds the viewer budget");
                if (ReplayPacketPolicy.allow(expected == ConnectionProtocol.CONFIGURATION, id.getNamespace(), id.getPath(), true))
                    packet.handle(connection.getPacketListener());
                var level=Minecraft.getInstance().level;
                if(level!=null&&(level.getEntityCount()>8192||level.getChunkSource().getLoadedChunksCount()>8192))
                    throw new IllegalArgumentException("Recorded world exceeds the viewer budget");
            } catch (RuntimeException error) { failure[0] = new IOException("Cannot apply replay packet", error); }
            finally { bytes.release(); }
        });
        if (failure[0] != null) throw failure[0];
    }

    public void render(Runnable action) { if (ready()) ReplayScope.run(this, action); else action.run(); }

    public void resize(int width, int height) {
        var window=state.game.gameRenderState().windowRenderState;
        if(window.width==width&&window.height==height)return;
        ReplayScope.run(this,()->{state.game.resize(width,height);window.width=width;window.height=height;});
    }

    public void snapInterpolations() {
        if(!ready())return;
        ReplayScope.run(this,()->{
            for(var entity:state.level.entitiesForRendering()) {
                var interpolation=entity.getInterpolation();
                if(!interpolation.hasActiveInterpolation())continue;
                var target=interpolation.target().immutable();interpolation.cancel();
                entity.setPos(target.position());entity.setYRot(target.yRot());entity.setXRot(target.xRot());entity.setOldPosAndRot();
            }
        });
    }

    @Override public void close() {
        if (closed) return;
        closed = true;
        ReplayScope.run(this, () -> {
            if (connection != null) connection.close();
            state.particles.clearParticles();
            state.extractor.setLevel(null);
            state.renderer.close(); state.game.close();
        });
    }
}
