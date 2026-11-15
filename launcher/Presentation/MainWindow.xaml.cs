using ToolLauncher.Mods.Catalog;
using ToolLauncher.Core.Operations;
using ToolLauncher.Presentation.Operations;
using ToolLauncher.Mods.Installation;
using System.ComponentModel;
using System.Diagnostics;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using Microsoft.Win32;
using ToolLauncher.Accounts;
using ToolLauncher.Core;
using ToolLauncher.Minecraft;
using ToolLauncher.Storage;
using ToolLauncher.Presentation.Accounts;
using ToolLauncher.Accounts.Profiles;

namespace ToolLauncher.Presentation;

public partial class MainWindow : Window
{
    private readonly LauncherServices services;
    private bool initializing = true;
    private bool busy;
    private bool gameRunning;
    private bool closeRequested;
    private bool updatingAccounts;
    private string page = "home";
    private int offset;
    private CancellationTokenSource? operation;
    private CancellationTokenSource? search;
    private Task? gameCompletion;
    private readonly BackgroundWork profileWork = new();
    private CancellationTokenSource? installedRead;
    private LatestProgress<InstallProgress>? operationProgress;
    private bool windowClosed;
    private bool populatingSettings;
    public MainWindow(LauncherServices services)
    {
        this.services = services;
        InitializeComponent();
        MemorySlider.Minimum = MemoryBudget.MinimumMiB;
        MemorySlider.Maximum = services.Settings.Memory.MaximumMiB;
        VersionList.ItemsSource = GameTarget.All;
        VersionList.SelectedItem = GameTarget.All.SingleOrDefault(target => target.Minecraft == services.Settings.Current.SelectedVersion);
        SearchInput.TextChanged += (_, _) => SearchPlaceholder.Visibility = SearchInput.Text.Length == 0 ? Visibility.Visible : Visibility.Collapsed;
        PopulateSettings(); ConnectPreferenceEvents(); initializing = false; UpdateChrome();
        AccountImportOverlay.SettingsChanged += (_, _) => { PopulateAccountSettings(); UpdateChrome(); };
        Loaded += async (_, _) =>
        {
            if (services.Settings.LoadError != null) Notice(services.Settings.LoadError.Message + "\n" + services.Paths.Settings);
            if (services.Settings.LoadError == null && !services.Settings.Current.AccountImportCompleted) await AccountImportOverlay.OpenAsync(services);
        };
        Closing += OnClosing;
        Closed += (_, _) => { windowClosed = true; search?.Cancel(); installedRead?.Cancel(); AccountImportOverlay.Cancel(); };
    }
    private GameTarget Selected() => VersionList.SelectedItem as GameTarget ?? throw new InvalidOperationException(LauncherLanguage.Get("selectFirst"));
    private void UpdateChrome()
    {
        var settings = services.Settings.Current;
        RefreshAppearance();
        LanguageButton.Content = settings.Language.ToUpperInvariant();
        AccountName.Text = settings.UseMicrosoft ? settings.AccountName : settings.Nickname;
        AccountKind.Text = settings.UseMicrosoft ? "Microsoft" : LauncherLanguage.Get("accountLocal");
        RefreshAccountChoices();
        MemoryBudgetHint.Text = string.Format(LauncherLanguage.Get("memoryBudget"), (services.Settings.Memory.TotalMiB / 1024d).ToString("0.#"), (services.Settings.Memory.MaximumMiB / 1024d).ToString("0.#"));
        SessionTitle.Text = gameRunning ? LauncherLanguage.Get("running") : settings.SelectedVersion == null ? LauncherLanguage.Get("selectFirst") : "Minecraft " + settings.SelectedVersion + "  ·  ToolVisuals " + (BundledClient.Builds().FirstOrDefault(build => build.Minecraft == settings.SelectedVersion)?.Version ?? "—");
        bool signInRequired = settings.UseMicrosoft && string.IsNullOrEmpty(settings.RefreshToken);
        PlayButton.IsEnabled = !busy && !gameRunning && !signInRequired && settings.SelectedVersion != null;
        StatusText.Visibility = busy || signInRequired || gameRunning ? Visibility.Visible : Visibility.Collapsed;
        if (!busy) StatusText.Text = signInRequired ? LauncherLanguage.Get("accountSignInRequired")
            : gameRunning ? LauncherLanguage.Get("running") : "";
        VersionList.IsEnabled = !busy && !gameRunning;
        HomeAccountPicker.IsEnabled = AccountPicker.IsEnabled = !busy;
        SettingsFields.IsEnabled = AccountFields.IsEnabled = !busy;
        bool canModify = !busy && !gameRunning && settings.SelectedVersion != null;
        ModActions.IsEnabled = CatalogActions.IsEnabled = canModify;
        Resources["CanModifyMods"] = canModify;
        UpdatePreferencesState();
        LanguageButton.IsEnabled = !busy;
        CancelButton.Visibility = busy ? Visibility.Visible : Visibility.Collapsed;
        InstallBar.Visibility = busy ? Visibility.Visible : Visibility.Collapsed;
    }
    private async Task RunAsync(string name, Func<CancellationToken, Task> action)
    {
        if (busy) return;
        search?.Cancel(); installedRead?.Cancel();
        using var cancellation = new CancellationTokenSource(); operation = cancellation; busy = true;
        NoticeBorder.Visibility = Visibility.Collapsed;
        using var progress = new LatestProgress<InstallProgress>(Dispatcher, ApplyProgress);
        operationProgress = progress;
        InstallBar.IsIndeterminate = true; StatusText.Text = LauncherLanguage.Get("busy"); UpdateChrome();
        try { await action(cancellation.Token); }
        catch (OperationCanceledException) when (cancellation.IsCancellationRequested) { StatusText.Text = LauncherLanguage.Get("cancel"); }
        catch (ManualDownloadRequired manual) { Notice(manual.Message); OpenWebsite(manual.Website); }
        catch (Exception error) { services.Log.Error(name, error); Notice(LauncherLanguage.Get("error") + ": " + error.Message); }
        finally
        {
            progress.Dispose(); operationProgress = null; operation = null; busy = false; InstallBar.IsIndeterminate = false; UpdateChrome();
            if (closeRequested) { closeRequested = false; Close(); }
            else if (page == "installed") await RefreshInstalledAsync();
            else if (page == "mods") await SearchAsync();
        }
    }
    private IProgress<InstallProgress> Progress() => operationProgress ?? throw new InvalidOperationException("No launcher operation is active");
    private void ApplyProgress(InstallProgress value)
    {
        string stage = value.Stage switch { "java" => "javaDownload", "mods" => "modDownload", _ => value.Stage };
        StatusText.Text = LauncherLanguage.Get(stage) + (value.Total > 0 ? "  ·  " + value.Done + " / " + value.Total : "…");
        InstallBar.IsIndeterminate = value.Total == 0; InstallBar.Value = value.Fraction * 100;
    }
    private async void PlayClick(object sender, RoutedEventArgs e)
    {
        if (gameRunning || busy) return;
        await RunAsync("Prepare and launch Minecraft", async token =>
        {
            if (PreferencesChanged()) SavePreferences();
            GameTarget target = Selected();
            ProfileLease? lease = ProfileLease.Acquire(services.Paths.Profile(target.Minecraft));
            try
            {
                StatusText.Text = LauncherLanguage.Get("auth");
                GameAccount account = await services.Accounts.CurrentAsync(token);
                var settings = services.Settings.Current;
                var progress = Progress();
                InstalledGame installed = await profileWork.RunAsync(cancellation => services.Installer.PrepareAsync(target, settings, progress, cancellation), token);
                token.ThrowIfCancellationRequested();
                services.Presence?.Suspend(true);
                GameSession session;
                try { session = await profileWork.RunAsync(cancellation => { cancellation.ThrowIfCancellationRequested(); return Task.FromResult(services.Runner.Start(installed, account, settings, lease)); }, token); }
                catch { services.Presence?.Suspend(false); throw; }
                lease = null; gameRunning = true;
                gameCompletion = ObserveGameAsync(session);
                if (settings.MinimizeOnLaunch) WindowState = WindowState.Minimized;
            }
            finally { lease?.Dispose(); }
        });
    }
    private async Task ObserveGameAsync(GameSession session)
    {
        try { if (await session.CompletionAsync() != 0) Notice(LauncherLanguage.Get("exitError")); }
        catch (Exception error) { services.Log.Error("Monitor Minecraft process", error); Notice(error.Message); }
        finally { gameRunning = false; services.Presence?.Suspend(false); UpdateChrome(); }
    }
    private async void VersionChanged(object sender, SelectionChangedEventArgs e)
    {
        if (initializing || busy || gameRunning) return;
        TryAction("Select Minecraft version", () => { services.Settings.Save(services.Settings.Current with { SelectedVersion = (VersionList.SelectedItem as GameTarget)?.Minecraft }); services.Presence?.Refresh(); UpdateChrome(); search?.Cancel(); installedRead?.Cancel(); InstalledList.ItemsSource = CatalogList.ItemsSource = null; });
        if (page == "installed") await RefreshInstalledAsync();
        if (page == "mods") { offset = 0; await SearchAsync(); }
    }
    private void ClearVersionClick(object sender, RoutedEventArgs e) => VersionList.SelectedItem = null;
    private async void NavigationClick(object sender, RoutedEventArgs e) => await NavigateAsync((string)((Button)sender).Tag);
    private async Task NavigateAsync(string destination)
    {
        page = destination; search?.Cancel();
        RefreshAppearance();
        HomePage.Visibility = destination == "home" ? Visibility.Visible : Visibility.Collapsed;
        ModsPage.Visibility = destination == "mods" ? Visibility.Visible : Visibility.Collapsed;
        InstalledPage.Visibility = destination == "installed" ? Visibility.Visible : Visibility.Collapsed;
        SettingsPage.Visibility = destination == "settings" ? Visibility.Visible : Visibility.Collapsed;
        AccountsPage.Visibility = destination == "accounts" ? Visibility.Visible : Visibility.Collapsed;
        foreach (var button in new[] { HomeNav, ModsNav, InstalledNav, AccountsNav, SettingsNav })
        {
            bool selected = (string)button.Tag == destination;
            button.SetResourceReference(BackgroundProperty, selected ? "NavSelected" : "Sidebar");
            button.SetResourceReference(ForegroundProperty, selected ? "Accent" : "Muted");
        }
        if (destination == "installed" && !busy) await RefreshInstalledAsync();
        if (destination == "mods") { offset = 0; await SearchAsync(); }
    }
    private async Task SearchAsync()
    {
        if (initializing || busy) return;
        search?.Cancel(); installedRead?.Cancel();
        using var cancellation = new CancellationTokenSource(); search = cancellation;
        CatalogEmpty.Visibility = CatalogList.Items.Count == 0 ? Visibility.Visible : Visibility.Collapsed;
        NextCatalogPage.IsEnabled = PreviousCatalogPage.IsEnabled = false;
        if (VersionList.SelectedItem is not GameTarget target) { search = null; CatalogList.ItemsSource = null; CatalogEmpty.Visibility = Visibility.Visible; CatalogEmpty.Text = LauncherLanguage.Get("selectFirst"); return; }
        CatalogEmpty.Text = LauncherLanguage.Get("search");
        try
        {
            string query = SearchInput.Text.Trim();
            int requestedOffset = offset;
            var result = await services.Catalog.SearchAsync(query, target, requestedOffset, cancellation.Token);
            var available = await profileWork.RunAsync(token => Task.FromResult(services.Mods.AvailableProjects(result.Projects, target, token)), cancellation.Token);
            if (windowClosed || cancellation.IsCancellationRequested || search != cancellation || !Equals(VersionList.SelectedItem, target)) return;
            CatalogList.ItemsSource = available; CatalogEmpty.Text = LauncherLanguage.Get("emptyMods");
            CatalogEmpty.Visibility = available.Count == 0 ? Visibility.Visible : Visibility.Collapsed;
            NextCatalogPage.IsEnabled = result.HasNext; PreviousCatalogPage.IsEnabled = requestedOffset > 0;
            PageNumber.Text = (requestedOffset / 20 + 1).ToString();
        }
        catch (OperationCanceledException) when (cancellation.IsCancellationRequested) { }
        catch (Exception error) { services.Log.Error("Search unified mod catalog for " + target.Minecraft, error); if (windowClosed || search != cancellation || cancellation.IsCancellationRequested) return; CatalogList.ItemsSource = null; CatalogEmpty.Visibility = Visibility.Visible; CatalogEmpty.Text = LauncherLanguage.Get("catalogUnavailable"); Notice(CatalogEmpty.Text); }
        finally { if (search == cancellation) search = null; }
    }
    private async void SearchClick(object sender, RoutedEventArgs e) { offset = 0; await SearchAsync(); }
    private async void SearchKey(object sender, KeyEventArgs e) { if (e.Key == Key.Enter) { offset = 0; await SearchAsync(); e.Handled = true; } }
    private async void PreviousClick(object sender, RoutedEventArgs e) { offset = Math.Max(0, offset - 20); await SearchAsync(); }
    private async void NextClick(object sender, RoutedEventArgs e) { offset = Math.Min(10000, offset + 20); await SearchAsync(); }
    private async void InstallClick(object sender, RoutedEventArgs e)
    {
        if (busy || gameRunning) return;
        var project = (ModProject)((Button)sender).DataContext;
        var target = Selected();
        await RunAsync("Install " + project.Title, async token =>
        {
            var progress = Progress();
            await profileWork.RunAsync(cancellation => services.Mods.InstallAsync(services.Sources.Single(source => source.Name == project.Source), project, target, progress, cancellation), token);
            Notice(project.Title + " · " + LauncherLanguage.Get("done"));
        });
    }
    private async Task RefreshInstalledAsync()
    {
        installedRead?.Cancel();
        using var cancellation = new CancellationTokenSource(); installedRead = cancellation;
        var target = VersionList.SelectedItem as GameTarget;
        InstalledEmpty.Text = LauncherLanguage.Get(target == null ? "selectFirst" : "readingMods");
        InstalledEmpty.Visibility = InstalledList.Items.Count == 0 ? Visibility.Visible : Visibility.Collapsed;
        try
        {
            var installed = target == null ? [] : await profileWork.RunAsync(token => Task.FromResult(services.Mods.Installed(target, token)), cancellation.Token);
            if (windowClosed || installedRead != cancellation || cancellation.IsCancellationRequested || !Equals(VersionList.SelectedItem, target)) return;
            InstalledList.ItemsSource = installed;
            InstalledEmpty.Text = LauncherLanguage.Get(target == null ? "selectFirst" : "emptyInstalled");
            InstalledEmpty.Visibility = installed.Count == 0 ? Visibility.Visible : Visibility.Collapsed;
        }
        catch (OperationCanceledException) when (cancellation.IsCancellationRequested) { }
        catch (Exception error)
        {
            services.Log.Error("Read profile mods", error);
            if (!windowClosed && installedRead == cancellation && !cancellation.IsCancellationRequested) { InstalledEmpty.Text = LauncherLanguage.Get("error"); Notice(error.Message); }
        }
        finally { if (installedRead == cancellation) installedRead = null; }
    }
    private async void ImportClick(object sender, RoutedEventArgs e)
    {
        if (busy || gameRunning) return;
        var dialog = new OpenFileDialog { Filter = "Fabric mod (*.jar)|*.jar", Multiselect = false };
        if (dialog.ShowDialog(this) != true) return;
        var target = Selected();
        await RunAsync("Import Fabric mod", async token =>
        {
            await profileWork.RunAsync(cancellation => { cancellation.ThrowIfCancellationRequested(); services.Mods.Import(dialog.FileName, target); return Task.CompletedTask; }, token);
            Notice(LauncherLanguage.Get("done"));
        });
    }
    private async void ToggleModClick(object sender, RoutedEventArgs e) => await ChangeModAsync((ModInfo)((Button)sender).DataContext, false);
    private async void RemoveModClick(object sender, RoutedEventArgs e) => await ChangeModAsync((ModInfo)((Button)sender).DataContext, true);
    private async Task ChangeModAsync(ModInfo mod, bool remove)
    {
        if (busy || gameRunning) return;
        var target = Selected();
        await RunAsync(remove ? "Remove profile mod" : "Toggle profile mod", token => profileWork.RunAsync(cancellation =>
        {
            cancellation.ThrowIfCancellationRequested();
            if (remove) services.Mods.Remove(mod, target); else services.Mods.Toggle(mod, target);
            return Task.CompletedTask;
        }, token));
    }
    private async void RefreshModsClick(object sender, RoutedEventArgs e) { if (!busy) await RefreshInstalledAsync(); }
    private async void SignInClick(object sender, RoutedEventArgs e)
    {
        await RunAsync("Microsoft account sign-in", async token =>
        {
            SavePreferences();
            await services.Accounts.SignInAsync(code => { Dispatcher.Invoke(() => { Notice(code.Code + "\n" + code.Message); OpenWebsite(code.VerificationUri); }); }, token);
            UpdateChrome(); Notice(services.Settings.Current.AccountName + " · Microsoft");
        });
    }
    private void SignOutClick(object sender, RoutedEventArgs e) => TryAction("Switch to local account", () => { services.AccountLibrary.UseLocal(NicknameInput.Text.Trim()); PopulateAccountSettings(); UpdateChrome(); });
    private void RefreshAccountChoices()
    {
        updatingAccounts = true;
        try
        {
            IReadOnlyList<SavedAccountChoice> choices = services.AccountLibrary.Profiles.Select(profile => new SavedAccountChoice(profile, profile.Name + " · " + (profile.Kind == ToolLauncher.Accounts.Profiles.AccountKind.Microsoft ? "Microsoft" : LauncherLanguage.Get("accountLocal")))).ToArray();
            if (AccountPicker.ItemsSource is IReadOnlyList<SavedAccountChoice> previous && previous.SequenceEqual(choices)) choices = previous;
            else AccountPicker.ItemsSource = HomeAccountPicker.ItemsSource = choices;
            var active = choices.FirstOrDefault(choice => choice.Profile.Identity == services.AccountLibrary.ActiveIdentity);
            AccountPicker.SelectedItem = HomeAccountPicker.SelectedItem = active;
            AccountSelectionHint.Text = active == null ? "" : active.Profile.RequiresSignIn ? LauncherLanguage.Get("accountSignInRequired") : active.Profile.Source;
        }
        finally { updatingAccounts = false; }
    }
    private void AccountSelected(object sender, SelectionChangedEventArgs e)
    {
        if (initializing || busy || updatingAccounts || ((ComboBox)sender).SelectedItem is not SavedAccountChoice selected) return;
        TryAction("Select saved launcher account", () => { services.AccountLibrary.Select(selected.Profile.Identity); PopulateAccountSettings(); UpdateChrome(); });
    }
    private void RemoveAccountClick(object sender, RoutedEventArgs e)
    {
        if (AccountPicker.SelectedItem is not SavedAccountChoice selected) return;
        TryAction("Remove saved launcher account", () => { services.AccountLibrary.Remove(selected.Profile.Identity); PopulateAccountSettings(); UpdateChrome(); });
    }
    private async void AccountImportClick(object sender, RoutedEventArgs e)
    {
        if (!busy) await AccountImportOverlay.OpenAsync(services);
    }
    private void SaveSettingsClick(object sender, RoutedEventArgs e) { if (!busy) TryAction("Save launcher settings", () => { SavePreferences(); Notice(LauncherLanguage.Get("saved")); }); }
    private void MemoryChanged(object sender, RoutedPropertyChangedEventArgs<double> e) { if (MemoryLabel != null) MemoryLabel.Text = (e.NewValue / 1024).ToString("0.##") + " " + LauncherLanguage.Get("gigabytes"); }
    private void JavaBrowseClick(object sender, RoutedEventArgs e)
    {
        var dialog = new OpenFileDialog { Filter = "Java (java.exe; javaw.exe)|java.exe;javaw.exe" };
        if (dialog.ShowDialog(this) == true) { if ((string)((Button)sender).Tag == "21") Java21Input.Text = dialog.FileName; else Java25Input.Text = dialog.FileName; }
    }
    private void LanguageClick(object sender, RoutedEventArgs e) => TryAction("Change launcher language", () => { string language = services.Settings.Current.Language == "ru" ? "en" : "ru"; services.Settings.Save(services.Settings.Current with { Language = language }); LauncherLanguage.Apply(language); services.Presence?.Refresh(); UpdateChrome(); });
    private void TryAction(string operationName, Action action)
    {
        try { action(); }
        catch (Exception error) { services.Log.Error(operationName, error); Notice(error.Message); }
    }
    private void Notice(string text) { NoticeText.Text = services.Log.Redact(text); NoticeBorder.Visibility = Visibility.Visible; }
    private void DismissNoticeClick(object sender, RoutedEventArgs e) => NoticeBorder.Visibility = Visibility.Collapsed;
    private void CancelClick(object sender, RoutedEventArgs e) => operation?.Cancel();
    private void OnClosing(object? sender, CancelEventArgs e)
    {
        if (busy) { e.Cancel = true; closeRequested = true; operation?.Cancel(); return; }
        if (PreferencesChanged() && MessageBox.Show(this, LauncherLanguage.Get("discardOnClose"), "ToolLauncher", MessageBoxButton.YesNo, MessageBoxImage.Question, MessageBoxResult.No) != MessageBoxResult.Yes) e.Cancel = true;
    }
    private void OpenWebsite(string url)
    {
        TryAction("Open project website", () => { var uri = new Uri(url); if (uri.Scheme != "https") throw new InvalidDataException("Only HTTPS websites may be opened"); Process.Start(new ProcessStartInfo(uri.AbsoluteUri) { UseShellExecute = true }); });
    }
    private void OpenFolder(string folder) => TryAction("Open launcher folder", () => { Directory.CreateDirectory(folder); Process.Start(new ProcessStartInfo("explorer.exe") { UseShellExecute = true, Arguments = '"' + folder + '"' }); });
    private void ModWebsiteClick(object sender, RoutedEventArgs e) => OpenWebsite(((ModProject)((Button)sender).DataContext).Website);
    private void CommunityClick(object sender, RoutedEventArgs e) => OpenWebsite("https://t.me/toolvisuals");
    private void SourceClick(object sender, RoutedEventArgs e) => OpenWebsite("https://github.com/2twofactorauth3-star/ToolVisuals");
    private void ProfileFolderClick(object sender, RoutedEventArgs e) => TryAction("Open Minecraft profile", () => OpenFolder(services.Paths.Profile(Selected().Minecraft)));
    private void LogsFolderClick(object sender, RoutedEventArgs e) => OpenFolder(services.Paths.Logs);
    private async void AccountSettingsClick(object sender, RoutedEventArgs e) => await NavigateAsync("accounts");
    private async void GameSettingsClick(object sender, RoutedEventArgs e) => await NavigateAsync("settings");
    private void HeaderDrag(object sender, MouseButtonEventArgs e) { if (e.ClickCount == 2) MaximizeClick(sender, e); else if (e.LeftButton == MouseButtonState.Pressed) DragMove(); }
    private void MinimizeClick(object sender, RoutedEventArgs e) => WindowState = WindowState.Minimized;
    private void MaximizeClick(object sender, RoutedEventArgs e) => WindowState = WindowState == WindowState.Maximized ? WindowState.Normal : WindowState.Maximized;
    private void CloseClick(object sender, RoutedEventArgs e) => Close();
}
