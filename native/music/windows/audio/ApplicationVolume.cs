using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;

namespace ToolVisuals.Media.Audio
{
    internal sealed class AudioOwner
    {
        internal readonly int Pid;
        internal readonly string Name, Path, Application;
        internal AudioOwner(int pid, string name, string path, string application) { Pid = pid; Name = Normalize(name); Path = Normalize(path); Application = Normalize(application); }
        internal static string Normalize(string value) { return (value ?? "").Trim().Replace('/', '\\').ToLowerInvariant(); }
        internal bool Matches(string source)
        {
            source = Normalize(source);
            if (source.Length == 0 || Name == "java" || Name == "javaw" || Name == "minecraft" || Name == "toolvisualsmedia") return false;
            if (Application.Length != 0 && source == Application || Path.Length != 0 && source == Path || source == Name || source == Name + ".exe") return true;
            string group = Group(source);
            return group.Length != 0 && group == Group(Name);
        }
        internal bool Browser { get { return BrowserGroup(Group(Name)); } }
        private static bool BrowserGroup(string group) { return group == "chrome" || group == "edge" || group == "firefox" || group == "brave" || group == "opera" || group == "yandexbrowser"; }
        private static string Group(string value)
        {
            if (value.EndsWith(".exe", StringComparison.Ordinal)) value = value.Substring(0, value.Length - 4);
            if (value == "chrome" || value == "google.chrome" || value.StartsWith("chrome._crx_", StringComparison.Ordinal)) return "chrome";
            if (value == "msedge" || value == "microsoft.msedge" || value.StartsWith("msedge._crx_", StringComparison.Ordinal)) return "edge";
            if (value == "firefox" || value == "mozilla.firefox" || value == "org.mozilla.firefox") return "firefox";
            if (value == "brave" || value == "bravesoftware.brave-browser" || value.StartsWith("brave._crx_", StringComparison.Ordinal)) return "brave";
            if (value == "opera" || value == "opera stable" || value == "opera gx stable") return "opera";
            if (value == "yandex" || value == "yandexbrowser" || value.StartsWith("yandex._crx_", StringComparison.Ordinal)) return "yandexbrowser";
            if (value == "spotify" || value.StartsWith("spotifyab.spotifymusic_", StringComparison.Ordinal) && value.EndsWith("!spotify", StringComparison.Ordinal)) return "spotify";
            if (value == "yandexmusic" || value == "yandex-music" || value == "yandex.music" || value == "ru.yandex.music" || value == "ru.yandex.desktop.music" || value == "yandex music" || value == "яндекс музыка") return "yandexmusic";
            if (value == "youtube-music" || value == "youtubemusic" || value == "com.github.th_ch.youtube_music") return "youtubemusic";
            if (value == "vlc" || value == "org.videolan.vlc") return "vlc";
            return "";
        }
    }

    internal interface IAudioChannel : IDisposable
    {
        AudioOwner Owner { get; }
        string Identity { get; }
        double Level { get; }
        bool Muted { get; }
        void SetLevel(float value);
        void SetMuted(bool value);
    }

    internal sealed class AudioVolumeState
    {
        internal static readonly AudioVolumeState Unavailable = new AudioVolumeState(-1, false, "", "");
        internal readonly double Level;
        internal readonly bool Muted;
        internal readonly string Target, Scope;
        internal bool Available { get { return Target.Length != 0; } }
        internal AudioVolumeState(double level, bool muted, string target, string scope) { Level = level; Muted = muted; Target = target; Scope = scope; }
    }

    internal sealed class ApplicationVolume
    {
        private readonly Func<List<IAudioChannel>> enumerate;
        private readonly int ownerPid, parentPid;
        internal ApplicationVolume(Func<List<IAudioChannel>> enumerate, int ownerPid, int parentPid) { this.enumerate = enumerate; this.ownerPid = ownerPid; this.parentPid = parentPid; }
        private List<IAudioChannel> Select(List<IAudioChannel> channels, string source)
        {
            return channels.Where(channel => channel.Owner.Pid != ownerPid && channel.Owner.Pid != parentPid && channel.Owner.Pid > 0 && channel.Owner.Matches(source)).ToList();
        }
        private static string Target(string source, List<IAudioChannel> channels)
        {
            if (channels.Count == 0) return "";
            string key = AudioOwner.Normalize(source) + "\0" + string.Join("\0", channels.Select(channel => channel.Owner.Pid + ":" + channel.Identity).OrderBy(value => value, StringComparer.Ordinal));
            using (var hash = SHA256.Create()) return BitConverter.ToString(hash.ComputeHash(Encoding.UTF8.GetBytes(key))).Replace("-", "").ToLowerInvariant();
        }
        internal AudioVolumeState Read(string source)
        {
            var channels = enumerate();
            try
            {
                var selected = Select(channels, source);
                if (selected.Count == 0) return AudioVolumeState.Unavailable;
                double level = selected.Max(channel => channel.Level);
                if (double.IsNaN(level) || double.IsInfinity(level) || level < 0 || level > 1) return AudioVolumeState.Unavailable;
                return new AudioVolumeState(level, selected.All(channel => channel.Muted), Target(source, selected), selected.Any(channel => channel.Owner.Browser) ? "browser" : "application");
            }
            finally { foreach (var channel in channels) channel.Dispose(); }
        }
        internal string Change(string source, string target, string action, double value)
        {
            if ((action != "volume" && action != "mute") || double.IsNaN(value) || double.IsInfinity(value) || value < 0 || value > 1 || action == "mute" && value != 0 && value != 1) return "invalid";
            var channels = enumerate();
            try
            {
                var selected = Select(channels, source);
                if (selected.Count == 0) return "unsupported";
                if (target.Length == 0 || Target(source, selected) != target) return "stale";
                foreach (var channel in selected) { if (action == "volume") channel.SetLevel((float)value); else channel.SetMuted(value == 1); }
                return "ok";
            }
            finally { foreach (var channel in channels) channel.Dispose(); }
        }
    }
}
