using System.ComponentModel;
using ToolLauncher.Diagnostics;
using System.Text.Json;
using System.Text.RegularExpressions;
using ToolLauncher.Core;
using ToolLauncher.Network;
using ToolLauncher.Storage;
using ToolLauncher.Minecraft.Runtime;
using ToolLauncher.Minecraft.Security;

namespace ToolLauncher.Minecraft;

public sealed class JavaRuntime(LauncherPaths paths, RemoteClient remote, LauncherLog log)
{
    public async Task<string> ResolveAsync(GameTarget target, LauncherSettings settings, IProgress<InstallProgress> progress, CancellationToken token)
    {
        string? installed = await FindInstalledAsync(target, settings, token);
        if (installed != null) return installed;
        if (!settings.AutomaticJava) throw new InvalidOperationException("Java " + target.Java + " was not found. Select java.exe in settings or enable automatic installation.");
        return await InstallLatestAsync(target.Java, progress, token, false).ConfigureAwait(false);
    }
    public async Task<string> InstallLatestAsync(int major, IProgress<InstallProgress> progress, CancellationToken token, bool requireSecurityFix = true)
    {
        if (major is not (21 or 25)) throw new InvalidDataException("Unsupported managed Java branch");
        string runtimes = Path.Combine(paths.Root, "runtimes", "java-" + major);
        progress.Report(new("java"));
        using var json = await remote.JsonAsync("https://api.adoptium.net/v3/assets/latest/" + major + "/hotspot?architecture=x64&image_type=jre&os=windows&vendor=eclipse", token);
        if (json.RootElement.GetArrayLength() == 0) throw new InvalidDataException("An updated Java runtime is not available yet");
        JsonElement latest = json.RootElement.EnumerateArray().First();
        string available = latest.GetProperty("version").GetProperty("openjdk_version").GetString()!;
        var advertised = JavaSecurityInfo.FromVersion(available, "Eclipse Adoptium");
        if (advertised.Major != major || requireSecurityFix && !advertised.Patched) throw new InvalidDataException("The latest available Java " + major + " has no confirmed Java4Shell fix yet. Decline the update to use mandatory protection and try updating later.");
        JsonElement package = latest.GetProperty("binary").GetProperty("package");
        string checksum = package.GetProperty("checksum").GetString()!;
        if (checksum.Length != 64 || !checksum.All(Uri.IsHexDigit)) throw new InvalidDataException("Java archive SHA-256 is invalid");
        string archive = Path.Combine(paths.Cache, "java-" + major + "-" + checksum[..16] + ".zip");
        await remote.DownloadAsync(new(RemoteClient.Trusted(package.GetProperty("link").GetString()!), checksum, "SHA256", package.GetProperty("size").GetInt64()), archive, token);
        string staging = Path.Combine(paths.Root, "runtimes", ".install-java-" + major + "-" + Guid.NewGuid().ToString("N"));
        try
        {
            await Task.Run(() => ArchiveFiles.Extract(archive, staging, false, token), token);
            string executable = Directory.EnumerateFiles(staging, "java.exe", SearchOption.AllDirectories).Single(path => Path.GetFileName(Path.GetDirectoryName(path)) == "bin");
            var information = await JavaSecurityInfo.ReadAsync(executable, token).ConfigureAwait(false);
            if (information.Major != major || requireSecurityFix && !information.Patched) throw new InvalidDataException("The downloaded Java has no confirmed Java4Shell fix. Keep protection enabled and try updating again later.");
            token.ThrowIfCancellationRequested();
            string destination = Path.Combine(runtimes, "managed-" + checksum[..16] + "-" + Guid.NewGuid().ToString("N"));
            string relative = Path.GetRelativePath(staging, executable);
            Directory.CreateDirectory(runtimes);
            Directory.Move(staging, destination);
            return Path.Combine(destination, relative);
        }
        finally { if (Directory.Exists(staging)) Directory.Delete(staging, true); }
    }
    public async Task<string?> FindInstalledAsync(GameTarget target, LauncherSettings settings, CancellationToken token)
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
            catch (Exception error) when (error is IOException or InvalidDataException or Win32Exception or InvalidOperationException) { log.Error("Inspect Java installation: " + candidate, error); }
        }
        return null;
    }
    public static async Task<int> MajorAsync(string executable, CancellationToken token)
    {
        var result = await JavaProcess.RunAsync(executable, ["-version"], token).ConfigureAwait(false);
        var match = Regex.Match(result.Output, "(?:java|openjdk) version \\\"(?:1\\.)?(\\d+)", RegexOptions.IgnoreCase);
        return result.ExitCode == 0 && match.Success ? int.Parse(match.Groups[1].Value) : throw new InvalidDataException("Could not identify Java version");
    }
}
