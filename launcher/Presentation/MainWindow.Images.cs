using System.Windows;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Threading;

namespace ToolLauncher.Presentation;

public partial class MainWindow
{
    private DispatcherTimer? heroResize;
    private int heroDecodeWidth;
    private void ConnectImageEvents()
    {
        heroResize = new DispatcherTimer(DispatcherPriority.Background, Dispatcher) { Interval = TimeSpan.FromMilliseconds(180) };
        heroResize.Tick += (_, _) => { heroResize.Stop(); RefreshHeroImage(); };
        HeroSurface.Loaded += (_, _) => RefreshHeroImage();
        HeroSurface.SizeChanged += (_, _) => QueueHeroImage();
        HeroSurface.IsVisibleChanged += (_, args) => { if ((bool)args.NewValue) RefreshHeroImage(); else heroResize.Stop(); };
        DpiChanged += (_, _) => QueueHeroImage();
        Closed += (_, _) => heroResize.Stop();
    }
    private static int HeroDecodeWidth(Size size, DpiScale dpi)
    {
        double pixels = Math.Max(size.Width * dpi.DpiScaleX, size.Height * 2 * dpi.DpiScaleY);
        return double.IsFinite(pixels) ? (int)Math.Clamp(Math.Ceiling(pixels / 128) * 128, 128, 1920) : 1920;
    }
    private bool CanDecodeHero() => !windowClosed && HeroSurface.IsVisible && HeroSurface.ActualWidth > 0 && HeroSurface.ActualHeight > 0;
    private void QueueHeroImage()
    {
        heroResize!.Stop();
        if (CanDecodeHero() && HeroDecodeWidth(HeroSurface.RenderSize, VisualTreeHelper.GetDpi(HeroSurface)) != heroDecodeWidth) heroResize.Start();
    }
    private void RefreshHeroImage()
    {
        if (!CanDecodeHero()) return;
        int width = HeroDecodeWidth(HeroSurface.RenderSize, VisualTreeHelper.GetDpi(HeroSurface));
        if (width == heroDecodeWidth) return;
        var image = new BitmapImage();
        image.BeginInit();
        image.CacheOption = BitmapCacheOption.OnLoad;
        image.DecodePixelWidth = width;
        image.UriSource = new Uri("pack://application:,,,/ToolLauncher;component/Assets/Minecraft-frozen-ocean.jpg", UriKind.Absolute);
        image.EndInit(); image.Freeze();
        HeroImageBrush.ImageSource = image; heroDecodeWidth = width;
    }
}
