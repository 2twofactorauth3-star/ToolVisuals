using ToolLauncher.Diagnostics;
using System.Buffers.Binary;
using System.IO.Pipes;
using System.Text.Json;
using ToolLauncher.Core;
using ToolLauncher.Storage;

namespace ToolLauncher.Discord;

public sealed class LauncherPresence : IAsyncDisposable
{
    public const string ApplicationId = "1564973463192735784";
    private readonly LauncherLog log;
    private readonly Func<LauncherSettings> settings;
    private readonly CancellationTokenSource shutdown = new();
    private readonly SemaphoreSlim writes = new(1);
    private readonly Task worker;
    private readonly long started = DateTimeOffset.UtcNow.ToUnixTimeSeconds();
    private volatile bool suspended;
    private int revision;
    public string Status { get; private set; } = "Waiting for Discord";
    public LauncherPresence(LauncherLog log, Func<LauncherSettings> settings)
    {
        this.log = log; this.settings = settings;
        worker = Task.Run(RunAsync);
    }
    public void Refresh() => Interlocked.Increment(ref revision);
    public void Suspend(bool value) { suspended = value; Refresh(); }
    private async Task RunAsync()
    {
        while (!shutdown.IsCancellationRequested)
        {
            NamedPipeClientStream? pipe = null;
            try
            {
                if (suspended) { Status = "Minecraft controls presence"; await Task.Delay(TimeSpan.FromSeconds(1), shutdown.Token); continue; }
                pipe = await ConnectAsync(shutdown.Token);
                if (pipe == null) { Status = "Discord is not running"; await Task.Delay(TimeSpan.FromSeconds(15), shutdown.Token); continue; }
                await WriteAsync(pipe, 0, JsonSerializer.SerializeToUtf8Bytes(new { v = 1, client_id = ApplicationId }), shutdown.Token);
                using (var deadline = CancellationTokenSource.CreateLinkedTokenSource(shutdown.Token))
                {
                    deadline.CancelAfter(TimeSpan.FromSeconds(8));
                    var ready = await ReadAsync(pipe, deadline.Token);
                    using var json = JsonDocument.Parse(ready.Data);
                    if (!json.RootElement.TryGetProperty("evt", out var evt) || evt.GetString() != "READY") throw new IOException("Discord did not acknowledge the IPC handshake");
                }
                Status = "Connected"; log.Info("Discord RPC connected for ToolLauncher");
                using var session = CancellationTokenSource.CreateLinkedTokenSource(shutdown.Token);
                Task reader = ReadLoopAsync(pipe, session.Token);
                DateTime sent = DateTime.MinValue; int lastRevision = -1;
                try
                {
                    while (!shutdown.IsCancellationRequested && !reader.IsCompleted && !suspended)
                    {
                        int current = Volatile.Read(ref revision);
                        if (current != lastRevision || DateTime.UtcNow - sent >= TimeSpan.FromMinutes(1))
                        {
                            object? activity = suspended ? null : Activity(settings());
                            await WriteAsync(pipe, 1, JsonSerializer.SerializeToUtf8Bytes(new { cmd = "SET_ACTIVITY", args = new { pid = Environment.ProcessId, activity }, nonce = Guid.NewGuid().ToString("N") }), shutdown.Token);
                            lastRevision = current; sent = DateTime.UtcNow;
                        }
                        await Task.Delay(TimeSpan.FromSeconds(1), shutdown.Token);
                    }
                    if (reader.IsCompleted) await reader;
                    if (!reader.IsFaulted && pipe.IsConnected)
                        await WriteAsync(pipe, 1, JsonSerializer.SerializeToUtf8Bytes(new { cmd = "SET_ACTIVITY", args = new { pid = Environment.ProcessId, activity = (object?)null }, nonce = Guid.NewGuid().ToString("N") }), CancellationToken.None);
                }
                finally
                {
                    session.Cancel(); pipe.Dispose();
                    try { await reader; } catch (OperationCanceledException) when (session.IsCancellationRequested) { } catch (IOException) when (session.IsCancellationRequested) { } catch (ObjectDisposedException) when (session.IsCancellationRequested) { }
                }
            }
            catch (OperationCanceledException) when (shutdown.IsCancellationRequested) { break; }
            catch (Exception error) when (error is IOException or TimeoutException or OperationCanceledException or InvalidOperationException or JsonException)
            {
                Status = "Reconnecting"; log.Error("Discord RPC session", error);
            }
            finally { pipe?.Dispose(); }
            try { await Task.Delay(TimeSpan.FromSeconds(suspended ? 1 : 15), shutdown.Token); } catch (OperationCanceledException) when (shutdown.IsCancellationRequested) { break; }
        }
        Status = "Stopped";
    }
    private object Activity(LauncherSettings preferences)
    {
        bool ru = preferences.Language == "ru";
        string? version = preferences.SelectedVersion;
        string name = "ToolLauncher" + (version == null ? "" : " " + version);
        return new { name, type = 0, status_display_type = 2, details = version == null ? (ru ? "В настройках лаунчера" : "Launcher settings") : name, state = version == null ? (ru ? "Выбирает версию Minecraft" : "Choosing a Minecraft version") : "Minecraft " + version + " · Fabric", assets = new { large_image = "toolvisuals_logo", large_text = name }, timestamps = new { start = started }, buttons = new[] { new { label = ru ? "Скачать ToolVisuals" : "Get ToolVisuals", url = "https://t.me/toolvisuals" } } };
    }
    private async Task ReadLoopAsync(NamedPipeClientStream pipe, CancellationToken token)
    {
        while (!token.IsCancellationRequested)
        {
            var frame = await ReadAsync(pipe, token);
            if (frame.Opcode == 2) throw new IOException("Discord closed the RPC session");
            if (frame.Opcode == 3) { await WriteAsync(pipe, 4, frame.Data, token); continue; }
            if (frame.Opcode == 1)
            {
                using var json = JsonDocument.Parse(frame.Data);
                if (json.RootElement.TryGetProperty("evt", out var evt) && evt.GetString() == "ERROR")
                {
                    Status = "Discord rejected activity";
                    string message = json.RootElement.TryGetProperty("data", out var data) && data.TryGetProperty("message", out var reason) ? reason.GetString() ?? "Unknown RPC error" : "Unknown RPC error";
                    log.Error("Discord SET_ACTIVITY", new IOException(message));
                }
            }
        }
    }
    private static async Task<NamedPipeClientStream?> ConnectAsync(CancellationToken token)
    {
        for (int index = 0; index < 10; index++)
        {
            var pipe = new NamedPipeClientStream(".", "discord-ipc-" + index, PipeDirection.InOut, PipeOptions.Asynchronous);
            try { await pipe.ConnectAsync(100, token); return pipe; }
            catch (TimeoutException) { pipe.Dispose(); }
            catch (IOException) { pipe.Dispose(); }
        }
        return null;
    }
    private async Task WriteAsync(Stream stream, int opcode, byte[] data, CancellationToken token)
    {
        if (data.Length > 4096) throw new InvalidDataException("Discord activity is too large");
        byte[] frame = new byte[8 + data.Length]; BinaryPrimitives.WriteInt32LittleEndian(frame, opcode); BinaryPrimitives.WriteInt32LittleEndian(frame.AsSpan(4), data.Length); data.CopyTo(frame, 8);
        using var timeout = CancellationTokenSource.CreateLinkedTokenSource(token); timeout.CancelAfter(TimeSpan.FromSeconds(5));
        await writes.WaitAsync(timeout.Token);
        try { await stream.WriteAsync(frame, timeout.Token); await stream.FlushAsync(timeout.Token); }
        finally { writes.Release(); }
    }
    private static async Task<(int Opcode, byte[] Data)> ReadAsync(Stream stream, CancellationToken token)
    {
        byte[] header = new byte[8]; await stream.ReadExactlyAsync(header, token);
        int opcode = BinaryPrimitives.ReadInt32LittleEndian(header); int length = BinaryPrimitives.ReadInt32LittleEndian(header.AsSpan(4));
        if (opcode is < 0 or > 4 || length is < 0 or > 65536) throw new InvalidDataException("Invalid Discord IPC frame");
        byte[] data = new byte[length]; await stream.ReadExactlyAsync(data, token); return (opcode, data);
    }
    public async ValueTask DisposeAsync()
    {
        suspended = true; Refresh();
        await Task.Delay(120).ConfigureAwait(false);
        shutdown.Cancel();
        try { await worker.ConfigureAwait(false); } catch (OperationCanceledException) { }
        writes.Dispose(); shutdown.Dispose();
    }
}
