package ru.he3hauka.toolvisuals.client.sound.library;

import com.mojang.blaze3d.audio.SoundBuffer;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.valueproviders.ConstantFloat;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;
import ru.he3hauka.toolvisuals.client.sound.SoundCustomizer.Channel;

public final class CustomSounds {
    private CustomSounds() {}
    public static void initialize() {
        try { ToolVisualsClient.SOUNDS.load(); }
        catch (IOException error) { ClientErrors.report("Load custom sound library; original preserved", error); }
    }
    public static SoundEvent event(Channel channel) {
        String id = ToolVisualsClient.SOUNDS.selected(channel);
        return id == null || ToolVisualsClient.SOUNDS.get(id) == null ? null : event(id);
    }
    public static SoundEvent event(String id) { return SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("toolvisuals", "custom/" + SoundIds.require(id))); }
    public static WeighedSoundEvents resolve(Identifier location) {
        if (!location.getNamespace().equals("toolvisuals") || !location.getPath().startsWith("custom/")) return null;
        var entry = ToolVisualsClient.SOUNDS.get(location.getPath().substring(7));
        if (entry == null) return null;
        var result = new WeighedSoundEvents(location, null);
        var path = Identifier.fromNamespaceAndPath("toolvisuals", "custom/" + entry.id() + "/" + entry.hash());
        result.addSound(new Sound(path, ConstantFloat.of(1), ConstantFloat.of(1), 1, Sound.Type.FILE, false, false, 16));
        return result;
    }
    public static boolean owns(Identifier path) {
        return path.getNamespace().equals("toolvisuals") && path.getPath().startsWith("sounds/custom/");
    }
    public static CompletableFuture<SoundBuffer> buffer(Identifier path, Map<Identifier, CompletableFuture<SoundBuffer>> cache) {
        return cache.computeIfAbsent(path, key -> CompletableFuture.supplyAsync(() -> {
            try {
                String name = key.getPath();
                if (!name.endsWith(".ogg")) throw new IOException("Invalid custom sound resource");
                String[] values = name.substring("sounds/custom/".length(), name.length() - 4).split("/");
                if (values.length != 2 || !SoundIds.valid(values[0])) throw new IOException("Invalid custom sound resource");
                return ToolVisualsClient.SOUNDS.buffer(values[0], values[1]);
            } catch (IOException | RuntimeException error) {
                ClientErrors.report("Decode custom sound buffer " + key, error);
                throw new CompletionException(error);
            }
        }));
    }
}
