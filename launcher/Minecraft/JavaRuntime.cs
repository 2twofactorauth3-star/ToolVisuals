using ToolLauncher.Diagnostics;
using System.Diagnostics;
using System.Text.Json;
using System.Text.RegularExpressions;
using ToolLauncher.Core;
using ToolLauncher.Network;
using ToolLauncher.Storage;

namespace ToolLauncher.Minecraft;

public sealed class JavaRuntime(LauncherPaths paths, RemoteClient remote, LauncherLog log)
{
    public async Task<string> ResolveAsync(GameTarget target, LauncherSettings settings, IProgress<InstallProgress> progress, CancellationToken token)
    {
        string configured = settings.JavaPath(target.Java);
        if (configured.Length > 0)
        {
            if (await MajorAsync(configured, token) < target.Java) throw new InvalidOperationException("Minecraft " + target.Minecraft + " requires Java " + target.Java + " or newer");
            return Path.GetFullPath(configured);
        }
        string runtimes = Path.Combine(paths.Root, "runtimes", "java-" + target.Java);
        var candidates = new List<string>();
        if (Directory.Exists(runtimes)) candidates.AddRange(Directory.EnumerateFiles(runtimes, "java.exe", SearchOption.AllDirectories));
        string? javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
        if (!string.IsNullOrEmpty(javaHome)) candidates.Add(Path.Combine(javaHome, "bin", "java.exe"));
        string programFiles = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles);
        foreach (string parent in new[] { Path.Combine(programFiles, "Java"), Path.Combine(programFiles, "Eclipse Adoptium"), Path.Combine(programFiles, "Microsoft") })
            if (Directory.Exists(parent)) foreach (string folder in Directory.EnumerateDirectories(parent, "*" + target.Java + "*")) candidates.Add(Path.Combine(folder, "bin", "java.exe"));
        foreach (string candidate in candidates.Distinct(StringComparer.OrdinalIgnoreCase))
        {
            if (!File.Exists(candidate)) continue;
            try { if (await MajorAsync(candidate, token) == target.Java) return candidate; }
            catch (Exception error) when (error is IOException or System.ComponentModel.Win32Exception or InvalidOperationException) { log.Error("Inspect Java installation: " + candidate, error); }
        }
        if (!settings.AutomaticJava) throw new InvalidOperationException("Java " + target.Java + " was not found. Select java.exe in settings or enable automatic installation.");
        progress.Report(new("java"));
        using var json = await remote.JsonAsync("https://api.adoptium.net/v3/assets/latest/" + target.Java + "/hotspot?architecture=x64&image_type=jre&os=windows&vendor=eclipse", token);
        JsonElement package = json.RootElement.EnumerateArray().First().GetProperty("binary").GetProperty("package");
        string checksum = package.GetProperty("checksum").GetString()!;
        string archive = Path.Combine(paths.Cache, "java-" + target.Java + "-" + checksum[..16] + ".zip");
        await remote.DownloadAsync(new(RemoteClient.Trusted(package.GetProperty("link").GetString()!), checksum, "SHA256", package.GetProperty("size").GetInt64()), archive, token);
        string staging = runtimes + "." + Guid.NewGuid().ToString("N");
        try
        {
            await Task.Run(() => ArchiveFiles.Extract(archive, staging, false, token), token);
            string executable = Directory.EnumerateFiles(staging, "java.exe", SearchOption.AllDirectories).Single(path => Path.GetFileName(Path.GetDirectoryName(path)) == "bin");
            if (await MajorAsync(executable, token) != target.Java) throw new InvalidDataException("Downloaded Java has an unexpected version");
            if (Directory.Exists(runtimes)) Directory.Move(runtimes, runtimes + ".previous-" + DateTime.UtcNow.Ticks);
            Directory.Move(staging, runtimes);
            return Directory.EnumerateFiles(runtimes, "java.exe", SearchOption.AllDirectories).Single(path => Path.GetFileName(Path.GetDirectoryName(path)) == "bin");
        }
        finally { if (Directory.Exists(staging)) Directory.Delete(staging, true); }
    }
    public static async Task<int> MajorAsync(string executable, CancellationToken token)
    {
        if (!File.Exists(executable)) throw new FileNotFoundException("Java executable does not exist", executable);
        var info = new ProcessStartInfo(executable) { UseShellExecute = false, CreateNoWindow = true, RedirectStandardError = true, RedirectStandardOutput = true };
        info.ArgumentList.Add("-version");
        using var process = Process.Start(info) ?? throw new IOException("Could not start Java");
        using var timeout = CancellationTokenSource.CreateLinkedTokenSource(token);
        timeout.CancelAfter(TimeSpan.FromSeconds(15));
        try
        {
            Task<string> stderr = process.StandardError.ReadToEndAsync(timeout.Token);
            Task<string> stdout = process.StandardOutput.ReadToEndAsync(timeout.Token);
            await process.WaitForExitAsync(timeout.Token);
            var match = Regex.Match(await stderr + await stdout, "(?:java|openjdk) version \\\"(?:1\\.)?(\\d+)", RegexOptions.IgnoreCase);
            return process.ExitCode == 0 && match.Success ? int.Parse(match.Groups[1].Value) : throw new InvalidDataException("Could not identify Java version");
        }
        finally { if (!process.HasExited) process.Kill(true); }
    }
}
