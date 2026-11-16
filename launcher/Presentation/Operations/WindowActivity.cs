using System.Windows;

namespace ToolLauncher.Presentation.Operations;

public static class WindowActivity
{
    public static readonly DependencyProperty ForegroundProperty = DependencyProperty.RegisterAttached("Foreground", typeof(bool), typeof(WindowActivity),
        new FrameworkPropertyMetadata(false, FrameworkPropertyMetadataOptions.Inherits));
    public static bool GetForeground(DependencyObject value) => (bool)value.GetValue(ForegroundProperty);
    public static void SetForeground(DependencyObject value, bool foreground) => value.SetValue(ForegroundProperty, foreground);
}
