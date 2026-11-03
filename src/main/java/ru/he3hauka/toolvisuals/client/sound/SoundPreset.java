package ru.he3hauka.toolvisuals.client.sound;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public enum SoundPreset {
    CLICK("click", "Click", () -> SoundEvents.UI_BUTTON_CLICK.value()),
    BELL("bell", "Bell", () -> SoundEvents.NOTE_BLOCK_BELL.value()),
    CHIME("chime", "Chime", () -> SoundEvents.AMETHYST_BLOCK_CHIME),
    POP("pop", "Pop", () -> SoundEvents.ITEM_PICKUP),
    SOFT("soft", "Soft", () -> SoundEvents.NOTE_BLOCK_HARP.value()),
    PLING("pling", "Pling", () -> SoundEvents.NOTE_BLOCK_PLING.value()),
    BIT("bit", "Bit", () -> SoundEvents.NOTE_BLOCK_BIT.value()),
    XYLOPHONE("xylophone", "Xylophone", () -> SoundEvents.NOTE_BLOCK_XYLOPHONE.value()),
    COW_BELL("cow_bell", "Cow Bell", () -> SoundEvents.NOTE_BLOCK_COW_BELL.value()),
    GUITAR("guitar", "Guitar", () -> SoundEvents.NOTE_BLOCK_GUITAR.value()),
    FLUTE("flute", "Flute", () -> SoundEvents.NOTE_BLOCK_FLUTE.value()),
    BASS("bass", "Bass", () -> SoundEvents.NOTE_BLOCK_BASS.value()),
    SNARE("snare", "Snare", () -> SoundEvents.NOTE_BLOCK_SNARE.value()),
    HAT("hat", "Hi Hat", () -> SoundEvents.NOTE_BLOCK_HAT.value()),
    LEVEL_UP("level_up", "Level Up", () -> SoundEvents.PLAYER_LEVELUP),
    TOAST("toast", "Toast", () -> SoundEvents.UI_TOAST_IN),
    EXPERIENCE("experience", "Experience", () -> SoundEvents.EXPERIENCE_ORB_PICKUP);

    private static final SoundPreset[] PRESETS = values();
    public static final int CUSTOM = PRESETS.length;
    public final String id, title;
    public final Supplier<SoundEvent> sound;
    SoundPreset(String id, String title, Supplier<SoundEvent> sound) { this.id = id; this.title = title; this.sound = sound; }
    public static String[] choices() { return java.util.stream.Stream.concat(Arrays.stream(PRESETS).map(p -> p.title), java.util.stream.Stream.of("Custom")).toArray(String[]::new); }
    public static boolean reserved(String id) { return id.equals("custom") || Arrays.stream(PRESETS).anyMatch(p -> p.id.equals(id)); }
    public static SoundEvent event(int index) { return PRESETS[Math.clamp(index, 0, PRESETS.length - 1)].sound.get(); }
}
