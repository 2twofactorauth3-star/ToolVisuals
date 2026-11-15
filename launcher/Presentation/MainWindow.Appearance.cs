using System.Windows;
using System.Windows.Controls.Primitives;
using ToolLauncher.Minecraft;
using ToolLauncher.Presentation.Operations;

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
        MemoryLabel.Text = (MemorySlider.Value / 1024d).ToString("0.##") + " " + LauncherLanguage.Get("gigabytes");
        double spacing = settings.CompactInterface ? 14 : 20;
        ContentArea.Margin = page == "home" ? new Thickness(spacing, 0, spacing, 14) : new Thickness(spacing + 6, 12, spacing + 6, 18);
        LaunchDock.Margin = new Thickness(spacing, 0, spacing, spacing);
        Resources["NavigationPadding"] = new Thickness(12, settings.CompactInterface ? 10 : 13, 12, settings.CompactInterface ? 10 : 13);
        bool animate = settings.InterfaceAnimations && SystemParameters.ClientAreaAnimation;
        HoverFeedback.SetEnabled(RootVisual, animate);
        Resources["PopupMotion"] = animate ? PopupAnimation.Fade : PopupAnimation.None;
        RefreshNavigation();
        ResizeHero(ActualHeight > 0 ? ActualHeight : Height);
    }

    private void RefreshNavigation()
    {
        foreach (var button in new[] { HomeNav, ModsNav, InstalledNav, AccountsNav, SettingsNav })
        {
            bool selected = (string)button.Tag == page;
            button.SetResourceReference(BackgroundProperty, selected ? "NavSelected" : "Sidebar");
            button.SetResourceReference(ForegroundProperty, selected ? "SelectedForeground" : "Muted");
        }
    }

    private void WindowSizeChanged(object sender, SizeChangedEventArgs e) => ResizeHero(e.NewSize.Height);

    private void ResizeHero(double height)
    {
        if (HeroTitleText == null) return;
        HeroTitleText.FontSize = height < 780 ? 54 : 66;
    }
}
