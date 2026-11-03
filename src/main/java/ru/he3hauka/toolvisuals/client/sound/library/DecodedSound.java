package ru.he3hauka.toolvisuals.client.sound.library;

import javax.sound.sampled.AudioFormat;

public record DecodedSound(AudioFormat format, byte[] pcm) {
    public DecodedSound {
        if (format == null || pcm == null || pcm.length == 0 || format.getChannels() < 1 || format.getChannels() > 2
                || format.getSampleSizeInBits() != 16 || format.isBigEndian() || !AudioFormat.Encoding.PCM_SIGNED.equals(format.getEncoding())
                || !Float.isFinite(format.getSampleRate()) || format.getSampleRate() < 8000 || format.getSampleRate() > 96000
                || pcm.length % (format.getChannels() * 2) != 0 || pcm.length > format.getSampleRate() * format.getChannels() * 2 * AudioDecoder.MAX_SECONDS)
            throw new IllegalArgumentException("Invalid or oversized decoded audio");
        pcm = pcm.clone();
    }
}
