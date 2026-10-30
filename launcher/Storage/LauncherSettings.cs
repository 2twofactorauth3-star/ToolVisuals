using ToolLauncher.Diagnostics;
using ToolLauncher.Minecraft;
using ToolLauncher.Core;

namespace ToolLauncher.Storage;

public sealed record LauncherSettings
{
    public string Language { get; init; } = "en";
    public string? SelectedVersion { get; init; }
    public string Nickname { get; init; } = "ToolVisuals_" + Random.Shared.Next(1000, 10000);
    public int MemoryMiB { get; init; } = 4096;
    public string Java21 { get; init; } = "";
    public string Java25 { get; init; } = "";
    public bool AutomaticJava { get; init; } = true;
    public string MicrosoftClientId { get; init; } = "";
    public bool UseMicrosoft { get; init; }
    public string AccountName { get; init; } = "";
    public string AccountId { get; init; } = "";
    public string RefreshToken { get; init; } = "";
    public string CurseForgeKey { get; init; } = "";
    public string JavaPath(int major) => major == 25 ? Java25 : Java21;
    public LauncherSettings Validate()
    {
        if (SelectedVersion != null) GameTarget.Find(SelectedVersion);
        if (Language is not ("ru" or "en")) throw new InvalidDataException("Unknown language");
        if (MemoryMiB is < 2048 or > 32768) throw new InvalidDataException("Memory must be between 2048 and 32768 MiB");
        return this;
    }
}

public sealed class SettingsStore
{
    private readonly LauncherPaths paths;
    private readonly LauncherLog log;
    public LauncherSettings Current { get; private set; }
    public Exception? LoadError { get; }
    public SettingsStore(LauncherPaths paths, LauncherLog log)
    {
        this.paths = paths; this.log = log;
        try { Current = AtomicJson.Read<LauncherSettings>(paths.Settings).Validate(); }
        catch (Exception error) when (error is IOException or UnauthorizedAccessException or System.Text.Json.JsonException or InvalidOperationException)
        {
            Current = new(); LoadError = error; log.Error("Read launcher settings; original file has been preserved", error);
        }
    }
    public void Save(LauncherSettings next)
    {
        if (LoadError != null) throw new InvalidOperationException("Settings could not be read. Move launcher.json aside before saving new settings.", LoadError);
        next.Validate();
        AtomicJson.Write(paths.Settings, next);
        Current = next;
    }
}
