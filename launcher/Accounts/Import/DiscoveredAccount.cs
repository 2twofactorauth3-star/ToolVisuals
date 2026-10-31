using ToolLauncher.Accounts.Profiles;

namespace ToolLauncher.Accounts.Import;

public sealed record DiscoveredAccount(string Name, AccountKind Kind, string Uuid, string Source, bool Preferred = false)
{
    public AccountProfile ToProfile() => new(Name, Kind, Uuid, Source);
    public string Identity => ToProfile().Identity;
}

public sealed record AccountDiscoveryResult(IReadOnlyList<DiscoveredAccount> Accounts, IReadOnlyList<string> Launchers, int UnreadableFiles);
