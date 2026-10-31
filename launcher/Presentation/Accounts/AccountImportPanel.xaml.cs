using System.ComponentModel;
using System.Windows;
using System.Windows.Controls;
using Microsoft.Win32;
using ToolLauncher.Accounts.Import;
using ToolLauncher.Core;

namespace ToolLauncher.Presentation.Accounts;

public partial class AccountImportPanel : UserControl
{
    private LauncherServices? services;
    private CancellationTokenSource? scan;
    private readonly List<ImportAccountRow> rows = [];
    private readonly HashSet<string> sources = new(StringComparer.OrdinalIgnoreCase);
    private int unreadable;
    public event EventHandler? SettingsChanged;

    public AccountImportPanel() => InitializeComponent();

    public async Task OpenAsync(LauncherServices launcherServices)
    {
        if (Visibility == Visibility.Visible) return;
        services = launcherServices;
        Visibility = Visibility.Visible;
        Focus();
        foreach (var row in rows) row.PropertyChanged -= SelectionChanged;
        rows.Clear(); sources.Clear(); unreadable = 0;
        AccountsList.ItemsSource = null; DefaultAccount.ItemsSource = null;
        ImportError.Visibility = Visibility.Collapsed;
        await DiscoverAsync(token => launcherServices.AccountDiscovery.ScanAsync(token));
    }

    public void Cancel() { scan?.Cancel(); }

    private async Task DiscoverAsync(Func<CancellationToken, Task<AccountDiscoveryResult>> action)
    {
        scan?.Cancel();
        using var cancellation = new CancellationTokenSource(); scan = cancellation;
        ScanProgress.Visibility = Visibility.Visible;
        DiscoveryStatus.Text = LauncherLanguage.Get("importScanning");
        ImportButton.IsEnabled = false;
        try
        {
            var result = await action(cancellation.Token);
            if (cancellation.IsCancellationRequested || scan != cancellation) return;
            foreach (var source in result.Launchers) sources.Add(source);
            unreadable += result.UnreadableFiles;
            foreach (var account in result.Accounts)
            {
                if (rows.Any(row => row.Account.Identity == account.Identity)) continue;
                var row = new ImportAccountRow(account); row.PropertyChanged += SelectionChanged; rows.Add(row);
            }
            AccountsList.ItemsSource = rows.ToArray();
            UpdateSelection(); UpdateStatus();
        }
        catch (OperationCanceledException) when (cancellation.IsCancellationRequested) { }
        catch (Exception error) { Report("Discover launcher accounts", error); }
        finally
        {
            if (scan == cancellation) { scan = null; ScanProgress.Visibility = Visibility.Collapsed; UpdateSelection(); }
        }
    }

    private void UpdateStatus()
    {
        DiscoveryStatus.Text = rows.Count == 0 ? LauncherLanguage.Get("importNotFound") : string.Format(LauncherLanguage.Get("importFound"), string.Join(", ", sources), rows.Count);
        if (unreadable > 0) DiscoveryStatus.Text += "\n" + string.Format(LauncherLanguage.Get("importUnreadable"), unreadable);
        EmptyHint.Visibility = rows.Count == 0 ? Visibility.Visible : Visibility.Collapsed;
    }

    private void SelectionChanged(object? sender, PropertyChangedEventArgs e) => UpdateSelection();

    private void UpdateSelection()
    {
        string? current = (DefaultAccount.SelectedItem as ImportAccountRow)?.Account.Identity;
        var selected = rows.Where(row => row.Selected).ToArray();
        DefaultAccount.ItemsSource = selected;
        DefaultAccount.SelectedItem = selected.FirstOrDefault(row => row.Account.Identity == current) ?? selected.FirstOrDefault(row => row.Account.Preferred) ?? selected.FirstOrDefault();
        ImportButton.IsEnabled = scan == null && selected.Length > 0;
    }

    private async void FolderClick(object sender, RoutedEventArgs e)
    {
        if (services == null) return;
        var dialog = new OpenFolderDialog { Title = LauncherLanguage.Get("importFolder") };
        if (dialog.ShowDialog(Window.GetWindow(this)) == true) await DiscoverAsync(token => services.AccountDiscovery.ScanFolderAsync(dialog.FolderName, token));
    }

    private void ImportClick(object sender, RoutedEventArgs e)
    {
        if (services == null || DefaultAccount.SelectedItem is not ImportAccountRow active) return;
        try
        {
            services.AccountLibrary.Import(rows.Where(row => row.Selected).Select(row => row.Account).ToArray(), active.Account.Identity);
            Finish();
        }
        catch (Exception error) { Report("Import launcher account metadata", error); }
    }

    private void SkipClick(object sender, RoutedEventArgs e)
    {
        if (services == null) return;
        try { services.AccountLibrary.CompleteSetup(); Finish(); }
        catch (Exception error) { Report("Complete launcher account setup", error); }
    }

    private void Finish()
    {
        scan?.Cancel(); Visibility = Visibility.Collapsed; SettingsChanged?.Invoke(this, EventArgs.Empty);
    }

    private void LanguageClick(object sender, RoutedEventArgs e)
    {
        if (services == null) return;
        try
        {
            string language = LauncherLanguage.Current == "ru" ? "en" : "ru";
            services.Settings.Save(services.Settings.Current with { Language = language });
            LauncherLanguage.Apply(language); services.Presence?.Refresh(); UpdateStatus();
            foreach (var row in rows) row.RefreshLanguage();
            SettingsChanged?.Invoke(this, EventArgs.Empty);
        }
        catch (Exception error) { Report("Change account setup language", error); }
    }

    private void Report(string operation, Exception error)
    {
        services?.Log.Error(operation, error);
        ImportError.Text = services?.Log.Redact(error.Message) ?? error.Message;
        ImportError.Visibility = Visibility.Visible;
    }
}
