using System.Windows;
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
        HeroBuildLabel.Text = build == null ? "TOOLVISUALS" : "TOOLVISUALS " + build.Version;
        HeroTitleText.Text = settings.SelectedVersion is { } version ? "Minecraft " + version : "Minecraft";
        CurrentPageTitle.Text = LauncherLanguage.Get(page);
        var target = settings.SelectedVersion == null ? null : GameTarget.Find(settings.SelectedVersion);
        HomeRuntimeText.Text = (target == null ? "Java" : "Java " + target.Java) + "  ·  " + (settings.MemoryMiB / 1024d).ToString("0.##") + " " + LauncherLanguage.Get("gigabytes");
        MemoryLabel.Text = (MemorySlider.Value / 1024d).ToString("0.##") + " " + LauncherLanguage.Get("gigabytes");
        ContentArea.Margin = settings.CompactInterface ? new Thickness(22, 18, 22, 16) : new Thickness(28, 26, 28, 22);
        Resources["NavigationPadding"] = new Thickness(14, settings.CompactInterface ? 9 : 12, 14, settings.CompactInterface ? 9 : 12);
        Resources["VersionPadding"] = settings.CompactInterface ? new Thickness(14, 10, 14, 10) : new Thickness(17, 14, 17, 14);
        HoverFeedback.SetEnabled(RootVisual, settings.InterfaceAnimations && SystemParameters.ClientAreaAnimation);
        ResizeHero(ActualHeight > 0 ? ActualHeight : Height);
    }

    private void WindowSizeChanged(object sender, SizeChangedEventArgs e)
    {
        ResizeHero(e.NewSize.Height);
    }

    private void ResizeHero(double height)
    {
        if (HeroSurface == null || HeroTitleText == null) return;
        HeroSurface.Height = Math.Clamp(height - 610, 120, 180);
        HeroTitleText.FontSize = height < 780 ? 30 : 36;
    }
}
