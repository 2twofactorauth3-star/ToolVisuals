using System.Windows;
using System.Windows.Controls.Primitives;
using System.Windows.Interop;
using ToolLauncher.Presentation.Operations;

namespace ToolLauncher.Presentation;

public partial class MainWindow
{
    private bool progressIndeterminate;
    private void ConnectActivityEvents()
    {
        StateChanged += (_, _) => UpdateWindowActivity();
        SourceInitialized += (_, _) => UpdateWindowActivity();
        IsVisibleChanged += (_, _) => UpdateWindowActivity();
        Activated += (_, _) => UpdateWindowActivity();
        Deactivated += (_, _) => UpdateWindowActivity();
        UpdateWindowActivity();
    }
    private void UpdateWindowActivity()
    {
        bool visible = !windowClosed && IsVisible && WindowState != WindowState.Minimized;
        if (!windowClosed && PresentationSource.FromVisual(this) is HwndSource source && source.CompositionTarget is HwndTarget target)
        {
            var mode = WindowState == WindowState.Minimized ? RenderMode.SoftwareOnly : RenderMode.Default;
            if (target.RenderMode != mode) target.RenderMode = mode;
        }
        RootVisual.Visibility = visible ? Visibility.Visible : Visibility.Collapsed;
        WindowActivity.SetForeground(RootVisual, visible && IsActive);
        RefreshMotion();
    }
    private void RefreshMotion()
    {
        bool foreground = WindowActivity.GetForeground(RootVisual);
        bool animate = foreground && services.Settings.Current.InterfaceAnimations && SystemParameters.ClientAreaAnimation;
        HoverFeedback.SetEnabled(RootVisual, animate);
        var motion = animate ? PopupAnimation.Fade : PopupAnimation.None;
        if (!Equals(Resources["PopupMotion"], motion)) Resources["PopupMotion"] = motion;
        InstallBar.IsIndeterminate = foreground && busy && progressIndeterminate;
    }
}
