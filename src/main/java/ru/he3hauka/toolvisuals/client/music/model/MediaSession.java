package ru.he3hauka.toolvisuals.client.music.model;

import java.util.Locale;

public record MediaSession(String id, String app, String title, String artist, String album, String track,
                           String state, long position, long start, long end, long minimum, long maximum,
                           double rate, boolean shuffle, int repeat, int capabilities, MediaVolume volume) {
    public static final long MAX_TIME = 31_536_000_000L;
    public static final int PLAY=1, PAUSE=2, TOGGLE=4, PREVIOUS=8, NEXT=16, STOP=32,
            SEEK=64, SHUFFLE=128, REPEAT=256, RATE=512, CHANNEL_UP=1024, CHANNEL_DOWN=2048, FORWARD=4096, REWIND=8192,
            VOLUME=16384, MUTE=32768;

    public MediaSession {
        id = clean(id, 32); app = clean(app, 256); title = clean(title, 256);
        artist = clean(artist, 256); album = clean(album, 256); track = clean(track, 64); state = clean(state, 16);
        start = Math.clamp(start, 0, MAX_TIME); end = Math.clamp(end, start, MAX_TIME);
        position = Math.clamp(position, start, end);
        minimum = Math.clamp(minimum, start, end); maximum = Math.clamp(maximum, minimum, end);
        rate = Double.isFinite(rate) && Math.abs(rate) <= 16 ? rate : 1;
        repeat = Math.clamp(repeat, 0, 2); capabilities &= 65535;
        if (volume==null) volume=MediaVolume.UNAVAILABLE;
        if (!volume.available()) capabilities &= ~(VOLUME|MUTE);
        if (maximum <= minimum) capabilities &= ~SEEK;
    }

    public MediaSession(String id,String app,String title,String artist,String album,String track,String state,
                        long position,long start,long end,long minimum,long maximum,double rate,boolean shuffle,int repeat,int capabilities) {
        this(id,app,title,artist,album,track,state,position,start,end,minimum,maximum,rate,shuffle,repeat,capabilities,MediaVolume.UNAVAILABLE);
    }

    public boolean playing() { return state.equals("playing"); }
    public boolean has(int capability) { return (capabilities & capability) != 0; }
    public long positionAt(long received, long now) {
        double elapsed = playing() ? Math.clamp((now - received) / 1_000_000d, 0, 5000) * rate : 0;
        return Math.clamp(position + (long)elapsed, start, end);
    }
    public double progress(long received, long now) {
        return end > start ? (double)(positionAt(received, now) - start) / (end - start) : 0;
    }
    public String source() {
        String value = app.toLowerCase(Locale.ROOT);
        if (value.contains("spotify")) return "Spotify";
        if (value.contains("yandexmusic") || value.contains("yandex.music") || value.contains("yandex-music")) return "Yandex Music";
        if (value.contains("youtube-music") || value.contains("youtubemusic")) return "YouTube Music";
        if (value.contains("msedge")) return "Microsoft Edge";
        if (value.contains("chrome")) return "Google Chrome";
        if (value.contains("firefox")) return "Firefox";
        if (value.contains("opera")) return "Opera";
        if (value.contains("brave")) return "Brave";
        if (value.contains("music.ui") || value.contains("zunemusic")) return "Windows Media Player";
        if (value.contains("vlc")) return "VLC";
        int separator = Math.max(app.lastIndexOf('!'), Math.max(app.lastIndexOf('/'), app.lastIndexOf('\\')));
        return clean(app.substring(separator + 1).replaceFirst("(?i)\\.exe$", ""), 48);
    }
    public static String time(long milliseconds) {
        long seconds = Math.max(0, milliseconds / 1000);
        String tail=(seconds%60<10?"0":"")+seconds%60;
        return seconds >= 3600 ? seconds/3600+":"+(seconds/60%60<10?"0":"")+seconds/60%60+":"+tail : seconds/60+":"+tail;
    }
    public static String clean(String value, int limit) {
        if (value == null) return "";
        var result = new StringBuilder(Math.min(value.length(), limit));
        for (int index = 0; index < value.length() && result.length() < limit;) {
            int cp = value.codePointAt(index); index += Character.charCount(cp);
            if (result.length() + Character.charCount(cp) > limit) break;
            if (Character.isISOControl(cp) || cp == 0xa7 || cp >= 0x202a && cp <= 0x202e || cp >= 0x2066 && cp <= 0x2069) result.append(' ');
            else if (cp < 0xd800 || cp > 0xdfff) result.appendCodePoint(cp);
        }
        return result.toString().strip();
    }
}
