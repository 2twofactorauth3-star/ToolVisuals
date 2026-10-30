package ru.he3hauka.toolvisuals.client.replay.playback;

import net.minecraft.client.Minecraft;

public final class ReplayScope {
    private static final ThreadLocal<ReplayScene> CURRENT = new ThreadLocal<>();
    private ReplayScope() {}
    public static boolean active() { return CURRENT.get() != null; }
    public static ReplayScene scene() { return CURRENT.get(); }

    public static void run(ReplayScene scene, Runnable action) {
        var client = Minecraft.getInstance();
        if (!client.isSameThread() || CURRENT.get() != null) throw new IllegalStateException("Replay scene scope must be exclusive on the client thread");
        var live = new ReplaySceneState(client);
        var camera = client.options.getCameraType();
        CURRENT.set(scene);
        try {
            scene.state().install(client);
            client.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
            action.run();
        } finally {
            try { scene.capture(client); }
            finally { live.install(client); client.options.setCameraType(camera); CURRENT.remove(); }
        }
    }
}
