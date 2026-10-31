using ToolLauncher.Accounts.Import;
using ToolLauncher.Storage;

namespace ToolLauncher.Accounts.Profiles;

public sealed class AccountLibrary(SettingsStore settings)
{
    public const int MaximumAccounts = 256;

    public IReadOnlyList<AccountProfile> Profiles => CaptureCurrent().ToArray();

    public string? ActiveIdentity
    {
        get
        {
            var current = CurrentProfile(settings.Current);
            return current?.Identity;
        }
    }

    private static AccountProfile? CurrentProfile(LauncherSettings current)
    {
        string name = current.UseMicrosoft ? current.AccountName : current.Nickname;
        if (!GameAccount.ValidNickname(name)) return null;
        return current.UseMicrosoft ? new(name, AccountKind.Microsoft, current.AccountId, "ToolLauncher", current.RefreshToken) : new(name, AccountKind.Local);
    }

    private List<AccountProfile> CaptureCurrent()
    {
        var profiles = settings.Current.SavedAccounts.ToList();
        var active = CurrentProfile(settings.Current);
        if (active != null)
        {
            int index = profiles.FindIndex(profile => profile.Identity == active.Identity);
            if (index < 0) profiles.Add(active);
            else if (active.RefreshToken.Length > 0) profiles[index] = profiles[index] with { Name = active.Name, RefreshToken = active.RefreshToken };
        }
        return profiles;
    }

    private static LauncherSettings Activate(LauncherSettings settings, AccountProfile active, IEnumerable<AccountProfile> profiles)
        => settings with
        {
            SavedAccounts = profiles.ToArray(),
            Nickname = active.Name,
            UseMicrosoft = active.Kind == AccountKind.Microsoft,
            AccountName = active.Kind == AccountKind.Microsoft ? active.Name : "",
            AccountId = active.Kind == AccountKind.Microsoft ? active.Uuid : "",
            RefreshToken = active.Kind == AccountKind.Microsoft ? active.RefreshToken : ""
        };

    public void Select(string identity)
    {
        var profiles = CaptureCurrent();
        var selected = profiles.SingleOrDefault(profile => profile.Identity == identity) ?? throw new InvalidOperationException("The saved account is no longer available");
        settings.Save(Activate(settings.Current, selected, profiles));
    }

    public void UseLocal(string nickname)
    {
        GameAccount.Offline(nickname);
        var profiles = CaptureCurrent();
        var local = new AccountProfile(nickname, AccountKind.Local);
        profiles.RemoveAll(profile => profile.Identity == local.Identity);
        profiles.Add(local);
        settings.Save(Activate(settings.Current, local, profiles));
    }

    public void SavePreferences(LauncherSettings next)
    {
        var profiles = CaptureCurrent();
        if (!next.UseMicrosoft)
        {
            var active = new AccountProfile(next.Nickname, AccountKind.Local).Validate();
            int index = profiles.FindIndex(profile => profile.Identity == active.Identity);
            if (index < 0) profiles.Add(active);
            next = Activate(next, active, profiles);
        }
        else next = next with { SavedAccounts = profiles.ToArray() };
        settings.Save(next);
    }

    public void RememberMicrosoft(string name, string uuid, string encryptedRefreshToken)
    {
        var account = new AccountProfile(name, AccountKind.Microsoft, uuid, "Microsoft", encryptedRefreshToken).Validate();
        var profiles = CaptureCurrent();
        profiles.RemoveAll(profile => profile.Identity == account.Identity || profile.Kind == AccountKind.Microsoft && profile.Name.Equals(name, StringComparison.OrdinalIgnoreCase) && profile.Uuid.Length == 0);
        profiles.Add(account);
        settings.Save(Activate(settings.Current, account, profiles));
    }

    public void Import(IReadOnlyList<DiscoveredAccount> accounts, string defaultIdentity)
    {
        if (accounts.Count == 0) throw new InvalidOperationException("Choose at least one account to import");
        var profiles = !settings.ExistedOnStartup && !settings.Current.AccountImportCompleted && settings.Current.SavedAccounts.Length == 0 ? new List<AccountProfile>() : CaptureCurrent();
        foreach (var found in accounts)
        {
            var imported = found.ToProfile().Validate();
            int index = profiles.FindIndex(profile => profile.Identity == imported.Identity);
            if (index < 0) profiles.Add(imported);
            else
            {
                string sources = AccountProfile.MergeSources(profiles[index].Source, imported.Source);
                profiles[index] = profiles[index] with { Source = sources };
            }
        }
        var active = profiles.SingleOrDefault(profile => profile.Identity == defaultIdentity) ?? throw new InvalidOperationException("Choose an imported starting account");
        settings.Save(Activate(settings.Current with { AccountImportCompleted = true }, active, profiles));
    }

    public void CompleteSetup() => settings.Save(settings.Current with { AccountImportCompleted = true, SavedAccounts = CaptureCurrent().ToArray() });

    public void Remove(string identity)
    {
        var profiles = CaptureCurrent();
        profiles.RemoveAll(profile => profile.Identity == identity);
        if (identity == ActiveIdentity)
        {
            var fallback = profiles.FirstOrDefault() ?? new AccountProfile(new LauncherSettings().Nickname, AccountKind.Local);
            if (profiles.Count == 0) profiles.Add(fallback);
            settings.Save(Activate(settings.Current, fallback, profiles));
        }
        else settings.Save(settings.Current with { SavedAccounts = profiles.ToArray() });
    }
}
