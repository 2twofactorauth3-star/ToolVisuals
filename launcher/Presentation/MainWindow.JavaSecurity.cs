using System.Windows.Threading;
using System.ComponentModel;
using System.Net.Http;
using ToolLauncher.Minecraft;
using ToolLauncher.Minecraft.Security;
using ToolLauncher.Presentation.Security;

namespace ToolLauncher.Presentation;

public partial class MainWindow
{
    private readonly HashSet<string> acknowledgedJava = new(StringComparer.OrdinalIgnoreCase);
    private async Task CheckJavaSecurityAsync(GameTarget? selected, CancellationToken token)
    {
        StatusText.Text = LauncherLanguage.Get("javaSecurityChecking");
        string? updateFailure = null;
        while (true)
        {
            var runtime = new JavaRuntime(services.Paths, services.Remote, services.Log);
            var targets = selected == null ? GameTarget.All.DistinctBy(target => target.Java) : [selected];
            var candidates = new List<(GameTarget Target, string Path, JavaSecurityInfo Information, string Identity)>();
            foreach (var target in targets)
            {
                var settings = services.Settings.Current;
                string? executable = await Task.Run(() => runtime.FindInstalledAsync(target, settings, token), token);
                if (executable == null) continue;
                var information = await Task.Run(() => JavaSecurityInfo.ReadAsync(executable, token), token);
                string identity = executable + "\n" + information.Version + "\n" + new FileInfo(executable).LastWriteTimeUtc.Ticks;
                if (!information.Patched && !acknowledgedJava.Contains(identity)) candidates.Add((target, executable, information, identity));
            }
            if (candidates.Count == 0) return;
            token.ThrowIfCancellationRequested();
            string description = string.Join("\n\n", candidates.Select(candidate => "Java " + candidate.Target.Java + " · " + candidate.Information.Version + "\n" + candidate.Path));
            if (updateFailure != null) description += "\n\n" + updateFailure;
            var dialog = new JavaSecurityDialog(description) { Owner = this };
            using var registration = token.Register(() => Dispatcher.BeginInvoke(DispatcherPriority.Normal, new Action(() => dialog.Close())));
            bool? update = dialog.ShowDialog();
            token.ThrowIfCancellationRequested();
            if (update == null) throw new OperationCanceledException(token);
            if (update == false)
            {
                foreach (var candidate in candidates) acknowledgedJava.Add(candidate.Identity);
                services.Log.Info("Java update declined; mandatory Base64 intrinsic protection will be verified before game launch");
                return;
            }
            try
            {
                foreach (var candidate in candidates)
                {
                    string executable = await Task.Run(() => runtime.InstallLatestAsync(candidate.Target.Java, Progress(), token), token);
                    var next = services.Settings.Current;
                    next = candidate.Target.Java == 25 ? next with { Java25 = executable } : next with { Java21 = executable };
                    services.Settings.Save(next);
                    if (candidate.Target.Java == 25) Java25Input.Text = executable; else Java21Input.Text = executable;
                }
                UpdateChrome();
                return;
            }
            catch (Exception error) when (error is IOException or InvalidDataException or Win32Exception or HttpRequestException or InvalidOperationException or UnauthorizedAccessException)
            {
                services.Log.Error("Update Java security runtimes", error);
                updateFailure = LauncherLanguage.Get("javaSecurityUpdateFailed");
            }
        }
    }
}
