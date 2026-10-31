using ToolLauncher.Diagnostics;
using ToolLauncher.Minecraft;
using ToolLauncher.Core;
using ToolLauncher.Accounts.Profiles;

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
    public bool AccountImportCompleted { get; init; }
    public AccountProfile[] SavedAccounts { get; init; } = [];
    public string JavaPath(int major) => major == 25 ? Java25 : Java21;
    public LauncherSettings Validate()
    {
        if (SelectedVersion != null) GameTarget.Find(SelectedVersion);
        if (Language is not ("ru" or "en")) throw new InvalidDataException("Unknown language");
        if (MemoryMiB < MemoryBudget.MinimumMiB) throw new InvalidDataException("Minecraft needs at least 1024 MiB of memory");
        if (SavedAccounts == null || SavedAccounts.Length > AccountLibrary.MaximumAccounts) throw new InvalidDataException("At most 256 accounts can be saved");
        foreach (var account in SavedAccounts) (account ?? throw new InvalidDataException("Saved account is empty")).Validate();
        if (SavedAccounts.Select(account => account.Identity).Distinct(StringComparer.Ordinal).Count() != SavedAccounts.Length) throw new InvalidDataException("Duplicate saved accounts");
        return this;
    }
}

public sealed class SettingsStore
{
    private readonly LauncherPaths paths;
    private readonly LauncherLog log;
    public MemoryBudget Memory { get; }
    public bool ExistedOnStartup { get; }
    public LauncherSettings Current { get; private set; }
    public Exception? LoadError { get; }
    public SettingsStore(LauncherPaths paths, LauncherLog log, MemoryBudget? memory = null)
    {
        this.paths = paths; this.log = log; Memory = memory ?? MemoryBudget.Detect(); Memory.EnsureSupported(); ExistedOnStartup = File.Exists(paths.Settings);
        try { var loaded = AtomicJson.Read<LauncherSettings>(paths.Settings).Validate(); Current = loaded with { MemoryMiB = Memory.Clamp(loaded.MemoryMiB) }; }
        catch (Exception error) when (error is IOException or UnauthorizedAccessException or System.Text.Json.JsonException or InvalidOperationException)
        {
            Current = new LauncherSettings { MemoryMiB = Memory.Clamp(4096) }; LoadError = error; log.Error("Read launcher settings; original file has been preserved", error);
        }
    }
    public void Save(LauncherSettings next)
    {
        if (LoadError != null) throw new InvalidOperationException("Settings could not be read. Move launcher.json aside before saving new settings.", LoadError);
        next = next.Validate() with { MemoryMiB = Memory.Clamp(next.MemoryMiB) };
        AtomicJson.Write(paths.Settings, next);
        Current = next;
    }
}
