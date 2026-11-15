using System.Windows;
using System.Windows.Controls;
using System.Windows.Media.Animation;
using System.Windows.Input;

namespace ToolLauncher.Presentation.Operations;

public static class HoverFeedback
{
    public static readonly DependencyProperty EnabledProperty = DependencyProperty.RegisterAttached("Enabled", typeof(bool), typeof(HoverFeedback), new FrameworkPropertyMetadata(true, FrameworkPropertyMetadataOptions.Inherits));
    public static readonly DependencyProperty TrackProperty = DependencyProperty.RegisterAttached("Track", typeof(bool), typeof(HoverFeedback), new PropertyMetadata(false, TrackChanged));

    public static bool GetEnabled(DependencyObject value) => (bool)value.GetValue(EnabledProperty);
    public static void SetEnabled(DependencyObject value, bool enabled) => value.SetValue(EnabledProperty, enabled);
    public static bool GetTrack(DependencyObject value) => (bool)value.GetValue(TrackProperty);
    public static void SetTrack(DependencyObject value, bool track) => value.SetValue(TrackProperty, track);

    private static void TrackChanged(DependencyObject value, DependencyPropertyChangedEventArgs args)
    {
        if (value is not Button button) return;
        if ((bool)args.NewValue) { button.MouseEnter += Enter; button.MouseLeave += Leave; }
        else { button.MouseEnter -= Enter; button.MouseLeave -= Leave; }
    }

    private static void Enter(object sender, MouseEventArgs args) => Apply((Button)sender, 0.055);
    private static void Leave(object sender, MouseEventArgs args) => Apply((Button)sender, 0);

    private static void Apply(Button button, double opacity)
    {
        if (button.Template?.FindName("Hover", button) is not Border hover) return;
        double previous = hover.HasAnimatedProperties ? hover.Opacity : opacity == 0 ? 0.055 : 0;
        hover.BeginAnimation(UIElement.OpacityProperty, null);
        if (!GetEnabled(button) || !button.IsEnabled) return;
        hover.BeginAnimation(UIElement.OpacityProperty, new DoubleAnimation(previous, opacity, TimeSpan.FromMilliseconds(110)) { FillBehavior = FillBehavior.Stop });
    }
}
