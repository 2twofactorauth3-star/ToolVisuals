using ToolLauncher.Diagnostics;
using ToolLauncher.Mods.Sources;
using ToolLauncher.Mods.Installation;
using ToolLauncher.Accounts;
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
    public ModManager Mods { get; }
    public MinecraftInstaller Installer { get; }
    public GameRunner Runner { get; }
    public LauncherPresence? Presence { get; }
    public IReadOnlyList<IModSource> Sources { get; }
    public LauncherServices(string? root = null, bool connectDiscord = true)
    {
        Paths = new(root); Log = new(Paths); Settings = new(Paths, Log); Remote = new(Log);
        Accounts = new(Settings, Log); Mods = new(Paths, Remote, Log); Installer = new(Paths, Remote, Log); Runner = new(Paths, Log);
        Sources = [new ModrinthSource(Remote), new CurseForgeSource(Remote, () => { string key = SecretProtection.Open(Settings.Current.CurseForgeKey); Log.Protect(key); return key; }), new GitHubSource(Remote)];
        if (connectDiscord) Presence = new(Log, () => Settings.Current);
    }
    public async ValueTask DisposeAsync()
    {
        if (Presence != null) await Presence.DisposeAsync();
        Accounts.Dispose(); Remote.Dispose();
    }
}
