package ru.he3hauka.toolvisuals.client.music;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;
import ru.he3hauka.toolvisuals.client.music.backend.MediaHelper;
import ru.he3hauka.toolvisuals.client.music.backend.MediaProtocol;
import ru.he3hauka.toolvisuals.client.music.model.MediaAction;
import ru.he3hauka.toolvisuals.client.music.model.MediaSession;
import ru.he3hauka.toolvisuals.client.music.model.MediaSnapshot;
import ru.he3hauka.toolvisuals.client.music.ui.MusicScreen;
import ru.he3hauka.toolvisuals.client.utility.preview.PreviewImages;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.MUSIC_HUD;

public final class MusicClient {
    private static final boolean SUPPORTED=MediaHelper.supported(System.getProperty("os.name",""),System.getProperty("os.version",""));
    private static MusicController controller;
    private static PreviewImages images;
    private static boolean requestOpen;
    private MusicClient() {}
    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(MusicClient::tick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client->close());
    }
    private static void tick(Minecraft client) {
        if (requestOpen) { requestOpen=false; open(client.gui.screen()); }
        boolean enabled=ToolVisualsClient.CONFIG.active(MUSIC_HUD);
        if (!enabled) { if (controller!=null) close(); return; }
        if (!SUPPORTED) return;
        if (controller==null) {
            var configDirectory=FabricLoader.getInstance().getConfigDir();
            controller=new MusicController(()->new ProcessBuilder(MediaHelper.extract(configDirectory).toString(),"--parent",Long.toString(ProcessHandle.current().pid())).start(),ClientErrors::report);
            controller.start();
        }
        controller.expire(System.nanoTime());
        var artwork=controller.pollArtwork();
        if (artwork!=null && artwork.image().key(artwork.generation()).equals(snapshot().artworkKey())) {
            String encoded=artwork.image().data();
            if (!encoded.isEmpty()) {
                if (images==null) images=new PreviewImages(128,2);
                images.request(artwork.image().key(artwork.generation()),()->{
                    try {
                        byte[] bytes=Base64.getDecoder().decode(encoded);
                        if (bytes.length>MediaProtocol.MAX_ARTWORK) throw new IOException("Media artwork exceeds its size limit");
                        return new ByteArrayInputStream(bytes);
                    } catch (IllegalArgumentException error) { throw new IOException("Invalid media artwork",error); }
                });
            }
        }
    }
    public static MediaSnapshot snapshot() { return controller==null?MediaSnapshot.EMPTY:controller.snapshot(); }
    public static String status() {
        if (!ToolVisualsClient.CONFIG.active(MUSIC_HUD)) return "disabled";
        if (!SUPPORTED) return "unsupported_os";
        if (controller==null) return "starting";
        return switch (controller.status()) { case STARTING->"starting";case READY->snapshot().selected()==null?"idle":"ready";case UNAVAILABLE,CLOSED->"unavailable"; };
    }
    public static String feedback() { return controller==null?"":controller.feedback(System.nanoTime()); }
    public static boolean command(MediaSnapshot snapshot,MediaSession session,MediaAction action,double value) { return ToolVisualsClient.CONFIG.available(MUSIC_HUD) && controller!=null && controller.command(snapshot,session,action,value); }
    public static boolean command(MediaAction action) { var snapshot=snapshot();return command(snapshot,snapshot.selected(),action,0); }
    public static void select(MediaSnapshot captured,MediaSession session) { command(captured,session,MediaAction.SELECT,0); }
    public static void seek(MediaSnapshot snapshot,MediaSession session,double fraction) {
        if (session==null || !Double.isFinite(fraction)) return;
        command(snapshot,session,MediaAction.SEEK,Math.clamp(session.start()+Math.clamp(fraction,0,1)*(session.end()-session.start()),session.minimum(),session.maximum()));
    }
    public static void drawArtwork(String key,GuiGraphicsExtractor g,float x,float y,float size) { if(images!=null) images.draw(key,g,x,y,size,size); }
    public static void requestOpen() { requestOpen=true; }
    public static void open(Screen parent) { Minecraft.getInstance().gui.setScreen(new MusicScreen(parent)); }
    public static void retry() { close(); }
    private static void close() {
        if (controller!=null) { controller.close(); controller=null; }
        if (images!=null) { images.close(); images=null; }
    }
}
