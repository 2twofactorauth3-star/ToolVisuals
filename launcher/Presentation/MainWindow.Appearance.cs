using System.Windows;
using ToolLauncher.Minecraft;

namespace ToolLauncher.Presentation;

public partial class MainWindow
{
    private void RefreshAppearance()
    {
        var settings = services.Settings.Current;
        var build = BundledClient.Builds().FirstOrDefault(item => item.Minecraft == settings.SelectedVersion)
            ?? BundledClient.Builds().FirstOrDefault();
        HeroBuildLabel.Text = build == null ? "Minecraft Java Edition" : string.Format(LauncherLanguage.Get("clientBuild"), build.Version);
        CurrentPageTitle.Text = LauncherLanguage.Get(page);
        var target = settings.SelectedVersion == null ? null : GameTarget.Find(settings.SelectedVersion);
        HomeRuntimeText.Text = (target == null ? "Java" : "Java " + target.Java) + " · " + (settings.MemoryMiB / 1024d).ToString("0.##") + " " + LauncherLanguage.Get("gigabytes");
        RefreshProfileSummary();
        MemoryLabel.Text = (MemorySlider.Value / 1024d).ToString("0.##") + " " + LauncherLanguage.Get("gigabytes");
        double spacing = settings.CompactInterface ? 18 : 24;
        ContentArea.Margin = page == "home" ? new Thickness(spacing, 0, spacing, 20) : new Thickness(spacing + 6, 12, spacing + 6, 18);
        LaunchDock.Margin = new Thickness(spacing, 0, spacing, spacing);
        bool home = page == "home";
        LaunchTitleText.Visibility = LaunchStatePanel.Visibility = home ? Visibility.Visible : Visibility.Collapsed;
        LaunchTitleGap.Height = new GridLength(home ? 20 : 0);
        LaunchDock.Padding = home ? new Thickness(24, 20, 24, 20) : new Thickness(18, 14, 18, 14);
        Resources["NavigationPadding"] = new Thickness(16, settings.CompactInterface ? 11 : 14, 16, settings.CompactInterface ? 11 : 14);
        RefreshMotion();
        RefreshNavigation();
        ResizeHero(ActualHeight > 0 ? ActualHeight : Height);
    }

    private void RefreshProfileSummary()
    {
        string? version = services.Settings.Current.SelectedVersion;
        HomeFolderHint.Text = version == null ? LauncherLanguage.Get("homeProfileHint") : "profiles / " + version;
        QuickFolderButton.ToolTip = version == null ? null : services.Paths.Profile(version);
        if (version == null) HomeModsHint.Text = LauncherLanguage.Get("selectFirst");
        else if (summaryVersion != version || summaryMods == null) HomeModsHint.Text = LauncherLanguage.Get(summaryVersion == version && summaryReadFailed ? "homeModsError" : "readingMods");
        else
        {
            int enabled = summaryMods.Count(mod => mod.Enabled);
            HomeModsHint.Text = summaryMods.Count == 0 ? LauncherLanguage.Get("homeModsEmpty") : string.Format(LauncherLanguage.Get("homeModsCount"), enabled, summaryMods.Count - enabled);
        }
    }

    private void RefreshNavigation()
    {
        foreach (var button in new[] { HomeNav, ModsNav, InstalledNav, AccountsNav, SettingsNav })
        {
            bool selected = (string)button.Tag == page;
            button.SetResourceReference(BackgroundProperty, selected ? "NavSelected" : "Sidebar");
            button.SetResourceReference(ForegroundProperty, selected ? "SelectedForeground" : "Muted");
            button.SetResourceReference(BorderBrushProperty, selected ? "Accent" : "TransparentBrush");
            button.FontWeight = selected ? FontWeights.SemiBold : FontWeights.Normal;
        }
    }

    private void WindowSizeChanged(object sender, SizeChangedEventArgs e) => ResizeHero(e.NewSize.Height);

    private void ResizeHero(double height)
    {
        if (HeroTitleText == null) return;
        HeroTitleText.FontSize = height < 780 ? 46 : 54;
    }
}
