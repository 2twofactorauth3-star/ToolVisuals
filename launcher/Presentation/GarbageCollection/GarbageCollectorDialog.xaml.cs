using System.Windows;
using ToolLauncher.Core;
using ToolLauncher.Minecraft;
using ToolLauncher.Minecraft.GarbageCollection;
using ToolLauncher.Storage;

namespace ToolLauncher.Presentation.GarbageCollection;

public partial class GarbageCollectorDialog : Window
{
    private CancellationTokenSource? inspection;
    private readonly LauncherServices services;
    private readonly GameTarget target;
    private readonly LauncherSettings settings;
    private readonly GarbageCollector collector;
    private bool closed;
    public GarbageCollectorDialog(LauncherServices services, GameTarget target, LauncherSettings settings, GarbageCollector collector)
    {
        this.services = services; this.target = target; this.settings = settings; this.collector = collector;
        InitializeComponent();
        var information = GarbageCollectorInfo.Find(collector);
        CollectorTitle.Text = collector == GarbageCollector.Auto ? LauncherLanguage.Get("gcAuto") : information.Name;
        Title = LauncherLanguage.Get("gcTitle") + " · " + CollectorTitle.Text;
        DescriptionText.Text = LauncherLanguage.Get(information.DescriptionKey);
        ProfileText.Text = "Minecraft " + target.Minecraft;
        Loaded += async (_, _) =>
        {
            MaxHeight = Math.Max(300, Math.Min(600, SystemParameters.WorkArea.Height - 48));
            MaxWidth = Math.Max(320, SystemParameters.WorkArea.Width - 48);
            CancelChoiceButton.Focus();
            using var cancellation = new CancellationTokenSource(); inspection = cancellation;
            try { await InspectAsync(cancellation.Token); }
            finally { inspection = null; }
        };
        Closed += (_, _) => { closed = true; inspection?.Cancel(); };
    }
    private async Task InspectAsync(CancellationToken token)
    {
        if (collector == GarbageCollector.Auto) { CompatibilityText.Text = LauncherLanguage.Get("gcAutomatic"); ConfirmButton.IsEnabled = true; return; }
        CompatibilityText.Text = LauncherLanguage.Get("gcChecking");
        try
        {
            var plan = await Task.Run(async () =>
            {
                string? executable = await new JavaRuntime(services.Paths, services.Remote, services.Log).FindInstalledAsync(target, settings, token);
                return executable == null ? null : await GarbageCollectorRuntime.PrepareAsync(executable, collector, token);
            }, token);
            if (plan == null)
            {
                if (!closed) { CompatibilityText.Text = LauncherLanguage.Get("gcDeferred"); ConfirmButton.IsEnabled = true; }
                return;
            }
            if (!closed) { CompatibilityText.Text = string.Format(LauncherLanguage.Get("gcSupported"), plan.JavaMajor); ConfirmButton.IsEnabled = true; }
        }
        catch (OperationCanceledException) when (token.IsCancellationRequested) { }
        catch (Exception error)
        {
            services.Log.Error("Inspect garbage collector for " + target.Minecraft, error);
            if (!closed) CompatibilityText.Text = LauncherLanguage.Get(error is GarbageCollectorException ? "gcUnavailable" : "gcInspectionFailed");
        }
    }
    private void ConfirmClick(object sender, RoutedEventArgs e) { if (ConfirmButton.IsEnabled) DialogResult = true; }
}
