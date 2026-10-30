using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Web.Script.Serialization;
using Windows.Foundation;
using Windows.Media.Control;
using Windows.Media;
using Windows.Storage.Streams;
using ToolVisuals.Media.Audio;

internal sealed class MediaBridge
{
    private const int MaximumSessions = 16, MaximumArtwork = 524288;
    private readonly JavaScriptSerializer json = new JavaScriptSerializer { MaxJsonLength = 786432, RecursionLimit = 16 };
    private readonly object outputLock = new object();
    private readonly ConcurrentQueue<Dictionary<string, object>> commands = new ConcurrentQueue<Dictionary<string, object>>();
    private readonly Dictionary<GlobalSystemMediaTransportControlsSession, Entry> entries = new Dictionary<GlobalSystemMediaTransportControlsSession, Entry>();
    private readonly Stopwatch clock = Stopwatch.StartNew();
    private volatile bool stopping;
    private int queued, changed = 1, nextSession;
    private long lastInput, lastSnapshot, nextSessions;
    private string selected = "";
    private string artworkSession = "", artworkTrack = "";
    private GlobalSystemMediaTransportControlsSessionManager manager;
    private StreamWriter output;
    private readonly SemaphoreSlim artworkSlot = new SemaphoreSlim(1, 1);
    private ApplicationVolume audio;
    private AudioVolumeState audioState = AudioVolumeState.Unavailable;
    private string audioSource = "";
    private long nextAudio;

    private sealed class Entry
    {
        internal string Id, App, Title = "", Artist = "", Album = "", Track = "";
        internal GlobalSystemMediaTransportControlsSession Session;
        internal volatile bool Retired;
        internal int MediaVersion = 1, ReadingVersion, PublishedVersion;
        internal long ReadStarted, RetryAt;
        internal IAsyncOperation<GlobalSystemMediaTransportControlsSessionMediaProperties> Pending;
        internal IRandomAccessStreamReference Thumbnail;
        internal TypedEventHandler<GlobalSystemMediaTransportControlsSession, MediaPropertiesChangedEventArgs> MediaHandler;
        internal TypedEventHandler<GlobalSystemMediaTransportControlsSession, PlaybackInfoChangedEventArgs> PlaybackHandler;
        internal TypedEventHandler<GlobalSystemMediaTransportControlsSession, TimelinePropertiesChangedEventArgs> TimelineHandler;
    }

    [MTAThread]
    private static int Main(string[] arguments)
    {
        var bridge = new MediaBridge();
        try { return bridge.Run(arguments); }
        catch (Exception error) { bridge.Error("connect", error); return 1; }
    }

    private int Run(string[] arguments)
    {
        output = new StreamWriter(Console.OpenStandardOutput(), new UTF8Encoding(false)) { AutoFlush = true };
        Console.SetError(new StreamWriter(Console.OpenStandardError(), new UTF8Encoding(false)) { AutoFlush = true });
        bool probe = arguments.Length == 1 && arguments[0] == "--probe";
        if (!probe && (arguments.Length != 2 || arguments[0] != "--parent")) throw new ArgumentException("Expected --parent PID");
        int parent = probe ? 0 : int.Parse(arguments[1], CultureInfo.InvariantCulture);
        int ownPid = Process.GetCurrentProcess().Id;
        audio = new ApplicationVolume(() => CoreAudio.Read(Error, ownPid, parent), ownPid, parent);
        lastInput = clock.ElapsedMilliseconds;
        if (!probe)
        {
            new Thread(ReadCommands) { IsBackground = true, Name = "ToolVisuals media input" }.Start();
            new Thread(delegate() {
                while (!stopping)
                {
                    try { using (var process = Process.GetProcessById(parent)) if (process.HasExited) break; }
                    catch (ArgumentException) { break; }
                    catch (Exception error) { Error("owner", error); break; }
                    if (clock.ElapsedMilliseconds - Interlocked.Read(ref lastInput) > 15000) break;
                    Thread.Sleep(1000);
                }
                stopping = true;
            }) { IsBackground = true, Name = "ToolVisuals media owner" }.Start();
        }
        manager = Await(GlobalSystemMediaTransportControlsSessionManager.RequestAsync(), 3000);
        if (probe) { Send(new { kind = "probe", version = 1, sessions = manager.GetSessions().Count }); return 0; }
        manager.SessionsChanged += SessionsChanged;
        manager.CurrentSessionChanged += CurrentChanged;
        Send(new { kind = "ready", version = 1 });
        try
        {
            while (!stopping)
            {
                if (clock.ElapsedMilliseconds >= nextSessions)
                {
                    RefreshSessions(); nextSessions = clock.ElapsedMilliseconds + 1000;
                }
                Dictionary<string, object> command;
                for (int count = 0; count < 4 && commands.TryDequeue(out command); count++)
                {
                    Interlocked.Decrement(ref queued); Handle(command);
                }
                foreach (var entry in entries.Values.ToArray()) PollMedia(entry);
                if (Interlocked.Exchange(ref changed, 0) != 0 || clock.ElapsedMilliseconds - lastSnapshot >= 1000)
                {
                    Publish(); lastSnapshot = clock.ElapsedMilliseconds;
                }
                Thread.Sleep(80);
            }
            return 0;
        }
        finally
        {
            manager.SessionsChanged -= SessionsChanged; manager.CurrentSessionChanged -= CurrentChanged;
            foreach (var entry in entries.Values) Retire(entry);
            entries.Clear();
        }
    }

    private void SessionsChanged(GlobalSystemMediaTransportControlsSessionManager sender, SessionsChangedEventArgs value) { Interlocked.Exchange(ref changed, 1); }
    private void CurrentChanged(GlobalSystemMediaTransportControlsSessionManager sender, CurrentSessionChangedEventArgs value) { Interlocked.Exchange(ref changed, 1); }

    private void RefreshSessions()
    {
        var sessions = manager.GetSessions().Take(MaximumSessions).ToArray();
        foreach (var previous in entries.Keys.ToArray()) if (!sessions.Contains(previous)) { Retire(entries[previous]); entries.Remove(previous); }
        foreach (var session in sessions)
        {
            if (entries.ContainsKey(session)) continue;
            var entry = new Entry { Id = (++nextSession).ToString(CultureInfo.InvariantCulture), App = Text(session.SourceAppUserModelId, 256), Session = session };
            entry.MediaHandler = delegate { if (!entry.Retired) { Interlocked.Increment(ref entry.MediaVersion); Interlocked.Exchange(ref changed, 1); } };
            entry.PlaybackHandler = delegate { if (!entry.Retired) Interlocked.Exchange(ref changed, 1); };
            entry.TimelineHandler = delegate { if (!entry.Retired) Interlocked.Exchange(ref changed, 1); };
            session.MediaPropertiesChanged += entry.MediaHandler;
            session.PlaybackInfoChanged += entry.PlaybackHandler;
            session.TimelinePropertiesChanged += entry.TimelineHandler;
            entries.Add(session, entry); Interlocked.Exchange(ref changed, 1);
        }
    }

    private void Retire(Entry entry)
    {
        entry.Retired = true;
        try
        {
            entry.Session.MediaPropertiesChanged -= entry.MediaHandler;
            entry.Session.PlaybackInfoChanged -= entry.PlaybackHandler;
            entry.Session.TimelinePropertiesChanged -= entry.TimelineHandler;
            if (entry.Pending != null) { entry.Pending.Cancel(); entry.Pending.Close(); entry.Pending = null; }
        }
        catch (Exception error) { Error("release", error); }
    }

    private void PollMedia(Entry entry)
    {
        try
        {
            if (entry.Pending != null)
            {
                if (entry.Pending.Status == AsyncStatus.Started)
                {
                    if (clock.ElapsedMilliseconds - entry.ReadStarted < 2500) return;
                    entry.Pending.Cancel(); throw new TimeoutException("Media metadata timed out");
                }
                var operation = entry.Pending; entry.Pending = null;
                try
                {
                    var properties = operation.GetResults();
                    if (entry.ReadingVersion != entry.MediaVersion) return;
                    entry.Title = Text(properties.Title, 256); entry.Artist = Text(properties.Artist, 256); entry.Album = Text(properties.AlbumTitle, 256);
                    entry.Thumbnail = properties.Thumbnail;
                    entry.Track = entry.Id + ":" + entry.ReadingVersion.ToString(CultureInfo.InvariantCulture);
                    entry.PublishedVersion = entry.ReadingVersion;
                    Interlocked.Exchange(ref changed, 1);
                }
                finally { operation.Close(); }
            }
            if (entry.ReadingVersion != entry.MediaVersion && clock.ElapsedMilliseconds >= entry.RetryAt)
            {
                entry.ReadingVersion = entry.MediaVersion; entry.ReadStarted = clock.ElapsedMilliseconds;
                entry.Pending = entry.Session.TryGetMediaPropertiesAsync();
            }
        }
        catch (Exception error)
        {
            if (entry.Pending != null) { try { entry.Pending.Close(); } catch (Exception closeError) { Error("release metadata", closeError); } entry.Pending = null; }
            entry.ReadingVersion = 0; entry.RetryAt = clock.ElapsedMilliseconds + 5000; Error("metadata", error);
        }
    }

    private Entry Current()
    {
        var pinned = entries.Values.FirstOrDefault(entry => entry.Id == selected);
        if (pinned != null) return pinned;
        selected = "";
        var current = manager.GetCurrentSession(); Entry found;
        if (current != null && entries.TryGetValue(current, out found)) return found;
        return entries.Values.FirstOrDefault();
    }

    private void Publish()
    {
        var current = Current(); var snapshots = new List<object>(); var published = new HashSet<string>();
        var currentAudio = current == null ? AudioVolumeState.Unavailable : ReadAudio(current.App);
        foreach (var entry in entries.Values.ToArray())
        {
            try
            {
                var info = entry.Session.GetPlaybackInfo(); var timeline = entry.Session.GetTimelineProperties(); var controls = info.Controls;
                long start = Milliseconds(timeline.StartTime), end = Milliseconds(timeline.EndTime);
                long minimum = Math.Max(start, Milliseconds(timeline.MinSeekTime)), maximum = Math.Min(end, Milliseconds(timeline.MaxSeekTime));
                long position = Milliseconds(timeline.Position); double rate = info.PlaybackRate ?? 1;
                if (double.IsNaN(rate) || double.IsInfinity(rate) || Math.Abs(rate) > 16) rate = 1;
                bool playing = info.PlaybackStatus == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Playing;
                double elapsed = (DateTimeOffset.UtcNow - timeline.LastUpdatedTime).TotalMilliseconds;
                if (playing && elapsed >= 0 && elapsed < 300000) position += (long)(elapsed * rate);
                if (end > start) position = Math.Max(start, Math.Min(end, position));
                int capabilities = (controls.IsPlayEnabled ? 1 : 0) | (controls.IsPauseEnabled ? 2 : 0) | (controls.IsPlayPauseToggleEnabled ? 4 : 0)
                    | (controls.IsPreviousEnabled ? 8 : 0) | (controls.IsNextEnabled ? 16 : 0) | (controls.IsStopEnabled ? 32 : 0)
                    | (controls.IsPlaybackPositionEnabled && maximum > minimum ? 64 : 0) | (controls.IsShuffleEnabled ? 128 : 0)
                    | (controls.IsRepeatEnabled ? 256 : 0) | (controls.IsPlaybackRateEnabled ? 512 : 0)
                    | (controls.IsChannelUpEnabled ? 1024 : 0) | (controls.IsChannelDownEnabled ? 2048 : 0)
                    | (controls.IsFastForwardEnabled ? 4096 : 0) | (controls.IsRewindEnabled ? 8192 : 0);
                var volume = entry == current ? currentAudio : AudioVolumeState.Unavailable;
                if (volume.Available) capabilities |= 16384 | 32768;
                snapshots.Add(new { id = entry.Id, app = entry.App, title = entry.Title, artist = entry.Artist, album = entry.Album,
                    track = entry.Track, state = info.PlaybackStatus.ToString().ToLowerInvariant(), position = position, start = start, end = end,
                    minimum = minimum, maximum = maximum, rate = rate, shuffle = info.IsShuffleActive ?? false,
                    repeat = (int)(info.AutoRepeatMode ?? MediaPlaybackAutoRepeatMode.None), capabilities = capabilities,
                    volume = volume.Level, muted = volume.Muted, volumeTarget = volume.Target, volumeScope = volume.Scope });
                published.Add(entry.Id);
            }
            catch (Exception error) { Error("snapshot", error); }
        }
        if (current != null && !published.Contains(current.Id)) current = null;
        Send(new { kind = "state", current = current == null ? "" : current.Id, pinned = published.Contains(selected) ? selected : "", sessions = snapshots });
        if (NeedsArtwork(current))
        {
            if (!artworkSlot.Wait(0)) return;
            artworkSession = current.Id; artworkTrack = current.Track;
            var thumbnail = current.Thumbnail; string track = current.Track, id = current.Id;
            ThreadPool.QueueUserWorkItem(delegate {
                try
                {
                    string encoded = "";
                    if (thumbnail != null)
                    {
                        using (var stream = Await(thumbnail.OpenReadAsync(), 2500))
                        {
                            if (stream.Size > MaximumArtwork) throw new InvalidDataException("Media artwork exceeds 512 KiB");
                            using (var input = stream.GetInputStreamAt(0)) using (var reader = new DataReader(input))
                            {
                                uint count = Await(reader.LoadAsync((uint)Math.Min(stream.Size, MaximumArtwork)), 2500);
                                var bytes = new byte[count]; reader.ReadBytes(bytes); encoded = Convert.ToBase64String(bytes);
                            }
                        }
                    }
                    if (!stopping && !current.Retired && current.Track == track) Send(new { kind = "art", id = id, track = track, data = encoded });
                }
                catch (Exception error) { Error("artwork", error); }
                finally { artworkSlot.Release(); }
            });
        }
    }

    private bool NeedsArtwork(Entry entry)
    {
        return entry != null && entry.Track.Length != 0 && (artworkSession != entry.Id || artworkTrack != entry.Track);
    }

    private AudioVolumeState ReadAudio(string source)
    {
        if (source == audioSource && clock.ElapsedMilliseconds < nextAudio) return audioState;
        audioSource = source; nextAudio = clock.ElapsedMilliseconds + 1000;
        try { audioState = audio.Read(source); }
        catch (Exception error) { audioState = AudioVolumeState.Unavailable; nextAudio += 4000; Error("audio mixer", error); }
        return audioState;
    }

    private static bool CanSeek(Entry entry, string track)
    {
        return entry.Track == track && entry.PublishedVersion == entry.MediaVersion;
    }

    private void Handle(Dictionary<string, object> command)
    {
        long request = Number(command, "request"); string action = Value(command, "action");
        if (action == "ping") return;
        if (action == "quit") { stopping = true; return; }
        try
        {
            if (action == "select")
            {
                string id = Value(command, "session");
                if (id.Length != 0 && !entries.Values.Any(value => value.Id == id)) { Reply(request, "stale"); return; }
                selected = id; Interlocked.Exchange(ref changed, 1); Reply(request, "ok"); return;
            }
            var entry = entries.Values.FirstOrDefault(value => value.Id == Value(command, "session"));
            if (entry == null) { Reply(request, "stale"); return; }
            if (action == "volume" || action == "mute")
            {
                Reply(request, audio.Change(entry.App, Value(command, "target"), action, Decimal(command, "value")));
                nextAudio = 0; Interlocked.Exchange(ref changed, 1); return;
            }
            var info = entry.Session.GetPlaybackInfo(); var controls = info.Controls;
            IAsyncOperation<bool> operation = null;
            switch (action)
            {
                case "toggle":
                    if (controls.IsPlayPauseToggleEnabled) operation = entry.Session.TryTogglePlayPauseAsync();
                    else if (info.PlaybackStatus == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Playing && controls.IsPauseEnabled) operation = entry.Session.TryPauseAsync();
                    else if (info.PlaybackStatus != GlobalSystemMediaTransportControlsSessionPlaybackStatus.Playing && controls.IsPlayEnabled) operation = entry.Session.TryPlayAsync();
                    break;
                case "previous": if (controls.IsPreviousEnabled) operation = entry.Session.TrySkipPreviousAsync(); else if (controls.IsChannelDownEnabled) operation = entry.Session.TryChangeChannelDownAsync(); break;
                case "next": if (controls.IsNextEnabled) operation = entry.Session.TrySkipNextAsync(); else if (controls.IsChannelUpEnabled) operation = entry.Session.TryChangeChannelUpAsync(); break;
                case "stop": if (controls.IsStopEnabled) operation = entry.Session.TryStopAsync(); break;
                case "forward": if (controls.IsFastForwardEnabled) operation = entry.Session.TryFastForwardAsync(); break;
                case "rewind": if (controls.IsRewindEnabled) operation = entry.Session.TryRewindAsync(); break;
                case "shuffle": if (controls.IsShuffleEnabled) operation = entry.Session.TryChangeShuffleActiveAsync(!(info.IsShuffleActive ?? false)); break;
                case "repeat": if (controls.IsRepeatEnabled) operation = entry.Session.TryChangeAutoRepeatModeAsync((MediaPlaybackAutoRepeatMode)(((int)(info.AutoRepeatMode ?? MediaPlaybackAutoRepeatMode.None) + 1) % 3)); break;
                case "rate":
                    double rate = Decimal(command, "value");
                    if (double.IsNaN(rate) || double.IsInfinity(rate) || rate < .25 || rate > 4) { Reply(request, "invalid"); return; }
                    if (controls.IsPlaybackRateEnabled) operation = entry.Session.TryChangePlaybackRateAsync(rate);
                    break;
                case "seek":
                    if (!CanSeek(entry, Value(command, "track"))) { Reply(request, "stale"); return; }
                    var timeline = entry.Session.GetTimelineProperties();
                    long minimum = Math.Max(Milliseconds(timeline.StartTime), Milliseconds(timeline.MinSeekTime));
                    long maximum = Math.Min(Milliseconds(timeline.EndTime), Milliseconds(timeline.MaxSeekTime));
                    if (controls.IsPlaybackPositionEnabled && maximum > minimum)
                    {
                        if (!CanSeek(entry, Value(command, "track"))) { Reply(request, "stale"); return; }
                        long position = Math.Max(minimum, Math.Min(maximum, Number(command, "value")));
                        operation = entry.Session.TryChangePlaybackPositionAsync(checked(position * 10000));
                    }
                    break;
                default: Reply(request, "invalid"); return;
            }
            if (operation == null) { Reply(request, "unsupported"); return; }
            Reply(request, Await(operation, 2000) ? "ok" : "rejected"); Interlocked.Exchange(ref changed, 1);
        }
        catch (Exception error) { Error("control", error); Reply(request, "failed"); }
    }

    private void ReadCommands()
    {
        try
        {
            var decoder = new JavaScriptSerializer { MaxJsonLength = 4096, RecursionLimit = 8 };
            using (var input = new StreamReader(Console.OpenStandardInput(), new UTF8Encoding(false, true)))
            {
                while (!stopping)
                {
                    var line = new StringBuilder(); int value;
                    while ((value = input.Read()) >= 0 && value != '\n') { if (line.Length >= 4096) throw new InvalidDataException("Media command limit"); if (value != '\r') line.Append((char)value); }
                    if (value < 0) break;
                    Interlocked.Exchange(ref lastInput, clock.ElapsedMilliseconds);
                    var command = decoder.Deserialize<Dictionary<string, object>>(line.ToString());
                    if (Interlocked.Increment(ref queued) > 16) { Interlocked.Decrement(ref queued); Reply(Number(command, "request"), "busy"); }
                    else commands.Enqueue(command);
                }
            }
        }
        catch (Exception error) { if (!stopping) Error("input", error); }
        finally { stopping = true; }
    }

    private static T Await<T>(IAsyncOperation<T> operation, int milliseconds)
    {
        var timer = Stopwatch.StartNew();
        try
        {
            while (operation.Status == AsyncStatus.Started)
            {
                if (timer.ElapsedMilliseconds >= milliseconds) { operation.Cancel(); throw new TimeoutException("Media operation timed out"); }
                Thread.Sleep(10);
            }
            return operation.GetResults();
        }
        finally { operation.Close(); }
    }

    private static long Milliseconds(TimeSpan value) { return Math.Max(0, Math.Min(31536000000L, value.Ticks / 10000)); }
    private static string Text(string value, int limit)
    {
        value = value ?? ""; var text = new StringBuilder();
        foreach (char character in value) { if (text.Length >= limit) break; text.Append(char.IsControl(character) ? ' ' : character); }
        if (text.Length > 0 && char.IsHighSurrogate(text[text.Length - 1])) text.Length--;
        return text.ToString().Trim();
    }
    private static string Value(Dictionary<string, object> value, string key) { object item; return value.TryGetValue(key, out item) && item is string ? (string)item : ""; }
    private static long Number(Dictionary<string, object> value, string key) { object item; return value.TryGetValue(key, out item) ? Convert.ToInt64(item, CultureInfo.InvariantCulture) : 0; }
    private static double Decimal(Dictionary<string, object> value, string key) { object item; return value.TryGetValue(key, out item) ? Convert.ToDouble(item, CultureInfo.InvariantCulture) : 0; }
    private void Reply(long request, string status) { Send(new { kind = "reply", request = request, status = status }); }
    private void Error(string operation, Exception error)
    {
        if (output == null) { Console.Error.WriteLine(error); return; }
        var win32 = error as System.ComponentModel.Win32Exception;
        string details = (win32 == null ? "" : "Win32 " + win32.NativeErrorCode + ": ") + error.ToString();
        Send(new { kind = "error", operation = operation, message = Text(details, 8192), code = error.HResult });
    }
    private void Send(object value)
    {
        lock (outputLock) { if (!stopping && output != null) output.WriteLine(json.Serialize(value)); }
    }
}
