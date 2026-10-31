using System.Windows;
using System.Windows.Input;
using System.Windows.Media.Animation;
using ToolLauncher.Minecraft;

namespace ToolLauncher.Presentation;

public partial class MainWindow
{
    private void RefreshAppearance()
    {
        var build = BundledClient.Builds().FirstOrDefault(item => item.Minecraft == services.Settings.Current.SelectedVersion)
            ?? BundledClient.Builds().FirstOrDefault();
        HeroBuildLabel.Text = build == null ? "TOOLVISUALS" : "TOOLVISUALS " + build.Version;
        CurrentPageTitle.Text = LauncherLanguage.Get(page);
    }

    private void WindowSizeChanged(object sender, SizeChangedEventArgs e)
    {
        ResizeHero(e.NewSize.Height);
    }

    private void ResizeHero(double height)
    {
        if (HeroSurface == null || HeroTitleText == null) return;
        HeroSurface.Height = Math.Clamp(height - 500, 240, 350);
        HeroTitleText.FontSize = height < 800 ? 38 : 46;
        HeroTitleText.LineHeight = HeroTitleText.FontSize + 6;
    }

    private void HeroMouseMove(object sender, MouseEventArgs e)
    {
        var point = e.GetPosition(HeroSurface);
        var center = new Point(point.X / Math.Max(1, HeroSurface.ActualWidth), point.Y / Math.Max(1, HeroSurface.ActualHeight));
        HeroLightBrush.Center = center;
        HeroLightBrush.GradientOrigin = center;
    }

    private void HeroMouseEnter(object sender, MouseEventArgs e) => SetHeroLight(1);
    private void HeroMouseLeave(object sender, MouseEventArgs e) => SetHeroLight(0);

    private void SetHeroLight(double opacity)
    {
        HeroPointerLight.BeginAnimation(OpacityProperty, new DoubleAnimation(opacity, TimeSpan.FromMilliseconds(180))
        {
            EasingFunction = new QuadraticEase { EasingMode = EasingMode.EaseOut }
        });
    }
}
