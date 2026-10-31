using System.ComponentModel;
using ToolLauncher.Accounts.Import;
using ToolLauncher.Accounts.Profiles;

namespace ToolLauncher.Presentation.Accounts;

public sealed class ImportAccountRow(DiscoveredAccount account) : INotifyPropertyChanged
{
    public DiscoveredAccount Account { get; } = account;
    public string Name => Account.Name;
    public string Source => Account.Source;
    public string Label => Name + " · " + (Microsoft ? "Microsoft" : LauncherLanguage.Get("accountLocal"));
    public bool Microsoft => Account.Kind == AccountKind.Microsoft;
    private bool selected = true;
    public bool Selected
    {
        get => selected;
        set { if (selected == value) return; selected = value; PropertyChanged?.Invoke(this, new(nameof(Selected))); }
    }
    public event PropertyChangedEventHandler? PropertyChanged;
    public void RefreshLanguage() => PropertyChanged?.Invoke(this, new(nameof(Label)));
}
