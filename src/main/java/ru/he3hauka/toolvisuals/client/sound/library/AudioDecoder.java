package ru.he3hauka.toolvisuals.client.sound.library;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import net.minecraft.client.sounds.JOrbisAudioStream;

public final class AudioDecoder {
    public static final int MAX_SECONDS = 10;
    public static final int MAX_SOURCE_BYTES = 16 * 1024 * 1024;
    public static final String EXTENSIONS = "ogg;wav;aiff;aif;au";
    private AudioDecoder() {}

    public static DecodedSound decode(Path path) throws IOException {
        if (!Files.isRegularFile(path) || Files.size(path) <= 0 || Files.size(path) > MAX_SOURCE_BYTES)
            throw new IOException("Audio file is missing, empty or exceeds 16 MiB");
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".ogg")) return ogg(path);
        if (!name.endsWith(".wav") && !name.endsWith(".aiff") && !name.endsWith(".aif") && !name.endsWith(".au"))
            throw new IOException("Supported audio: OGG Vorbis, WAV, AIFF, AU");
        try (var input = new BufferedInputStream(Files.newInputStream(path)); var audio = AudioSystem.getAudioInputStream(input)) {
            var source = audio.getFormat();
            int channels = source.getChannels(); float rate = source.getSampleRate();
            validateFormat(channels, rate);
            if (audio.getFrameLength() > (long)(rate * MAX_SECONDS)) throw new IOException("Audio is longer than 10 seconds");
            var format = new AudioFormat(rate, 16, channels, true, false);
            try (var converted = AudioSystem.getAudioInputStream(format, audio)) {
                int maximum = (int)(rate * MAX_SECONDS) * channels * 2;
                byte[] bytes = converted.readNBytes(maximum + channels * 2);
                if (bytes.length > maximum) throw new IOException("Audio is longer than 10 seconds");
                return checked(format, bytes);
            }
        } catch (UnsupportedAudioFileException | IllegalArgumentException error) { throw new IOException("Unsupported or damaged audio", error); }
    }

    private static DecodedSound ogg(Path path) throws IOException {
        try (var input = new BufferedInputStream(Files.newInputStream(path)); var audio = new JOrbisAudioStream(input)) {
            var format = audio.getFormat(); validateFormat(format.getChannels(), format.getSampleRate());
            int maximum = (int)(format.getSampleRate() * MAX_SECONDS) * format.getChannels() * 2;
            var bytes = new ByteArrayOutputStream();
            while (true) {
                ByteBuffer chunk = audio.read(8192);
                if (!chunk.hasRemaining()) break;
                if (bytes.size() + chunk.remaining() > maximum) throw new IOException("Audio is longer than 10 seconds");
                byte[] data = new byte[chunk.remaining()]; chunk.get(data); bytes.write(data);
            }
            return checked(format, bytes.toByteArray());
        } catch (IllegalArgumentException error) { throw new IOException("Unsupported or damaged OGG Vorbis audio", error); }
    }

    private static DecodedSound checked(AudioFormat format, byte[] bytes) throws IOException {
        try { return new DecodedSound(format, bytes); }
        catch (IllegalArgumentException error) { throw new IOException("Invalid decoded audio", error); }
    }
    private static void validateFormat(int channels, float rate) throws IOException {
        if (channels < 1 || channels > 2 || !Float.isFinite(rate) || rate < 8000 || rate > 96000)
            throw new IOException("Audio must be mono/stereo at 8000-96000 Hz");
    }
}
