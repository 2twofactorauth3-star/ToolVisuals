using ToolLauncher.Storage;
using System.Diagnostics;
using System.Windows;
using System.Windows.Threading;
using ToolLauncher.Core;
using ToolLauncher.Presentation;

namespace ToolLauncher;

public partial class App : Application
{
    private Mutex? singleInstance;
    private LauncherServices? services;
    protected override void OnStartup(StartupEventArgs e)
    {
        base.OnStartup(e);
        try
        {
            string? dataRoot = null;
            for (int index = 0; index < e.Args.Length; index++)
                if (e.Args[index] == "--data-root" && index + 1 < e.Args.Length) dataRoot = Path.GetFullPath(e.Args[++index]);
            var paths = new LauncherPaths(dataRoot);
            string identity = Convert.ToHexString(System.Security.Cryptography.SHA256.HashData(System.Text.Encoding.UTF8.GetBytes(paths.Root.ToLowerInvariant())))[..20];
            singleInstance = new Mutex(true, "Local\\ToolLauncher-" + identity, out bool created);
            if (!created) { MessageBox.Show("ToolLauncher is already running.", "ToolLauncher"); Shutdown(); return; }
            services = new(paths.Root);
            services.Log.Info("ToolLauncher 1.0.0 started");
            DispatcherUnhandledException += UiError;
            TaskScheduler.UnobservedTaskException += (_, args) => { services.Log.Error("Background task", args.Exception); args.SetObserved(); };
            AppDomain.CurrentDomain.UnhandledException += (_, args) => services.Log.Error("Unhandled launcher exception", args.ExceptionObject as Exception ?? new Exception(args.ExceptionObject.ToString()));
            LauncherLanguage.Apply(services.Settings.Current.Language);
            MainWindow = new MainWindow(services); MainWindow.Show();
        }
        catch (Exception error)
        {
            services?.Log.Error("Start ToolLauncher", error);
            MessageBox.Show(error.Message, "ToolLauncher", MessageBoxButton.OK, MessageBoxImage.Error); Shutdown(1);
        }
    }
    private void UiError(object sender, DispatcherUnhandledExceptionEventArgs e)
    {
        services?.Log.Error("Launcher interface", e.Exception);
        MessageBox.Show(e.Exception.Message, "ToolLauncher", MessageBoxButton.OK, MessageBoxImage.Error); e.Handled = true;
    }
    protected override void OnExit(ExitEventArgs e)
    {
        if (services != null)
        {
            try { services.DisposeAsync().AsTask().GetAwaiter().GetResult(); }
            catch (Exception error) { services.Log.Error("Close launcher services", error); }
        }
        singleInstance?.Dispose(); base.OnExit(e);
    }
}
