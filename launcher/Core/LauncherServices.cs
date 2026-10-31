using ToolLauncher.Diagnostics;
using ToolLauncher.Mods.Sources;
using ToolLauncher.Mods.Installation;
using ToolLauncher.Accounts;
using ToolLauncher.Accounts.Profiles;
using ToolLauncher.Accounts.Import;
using ToolLauncher.Mods.Catalog;
using ToolLauncher.Discord;
using ToolLauncher.Minecraft;
using ToolLauncher.Network;
using ToolLauncher.Storage;

namespace ToolLauncher.Core;

public sealed class LauncherServices : IAsyncDisposable
{
    public LauncherPaths Paths { get; }
    public LauncherLog Log { get; }
    public SettingsStore Settings { get; }
    public RemoteClient Remote { get; }
    public MicrosoftAccount Accounts { get; }
    public AccountLibrary AccountLibrary { get; }
    public AccountDiscovery AccountDiscovery { get; }
    public ModManager Mods { get; }
    public MinecraftInstaller Installer { get; }
    public GameRunner Runner { get; }
    public LauncherPresence? Presence { get; }
    public IReadOnlyList<IModSource> Sources { get; }
    public ModCatalog Catalog { get; }
    public LauncherServices(string? root = null, bool connectDiscord = true, MemoryBudget? memory = null)
    {
        Paths = new(root); Log = new(Paths); Settings = new(Paths, Log, memory); Remote = new(Log);
        AccountLibrary = new(Settings); AccountDiscovery = new(Log);
        Accounts = new(Settings, Log, AccountLibrary); Mods = new(Paths, Remote, Log); Installer = new(Paths, Remote, Log); Runner = new(Paths, Log);
        Sources = [new ModrinthSource(Remote), new CurseForgeSource(Remote, () => { string key = SecretProtection.Open(Settings.Current.CurseForgeKey); Log.Protect(key); return key; }), new GitHubSource(Remote)];
        Catalog = new(Sources, Log);
        if (connectDiscord) Presence = new(Log, () => Settings.Current);
    }
    public async ValueTask DisposeAsync()
    {
        if (Presence != null) await Presence.DisposeAsync().ConfigureAwait(false);
        Accounts.Dispose(); Remote.Dispose();
    }
}
