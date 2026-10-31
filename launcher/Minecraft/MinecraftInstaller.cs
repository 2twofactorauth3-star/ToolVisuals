using ToolLauncher.Diagnostics;
using System.Collections.Concurrent;
using System.Text.Json;
using ToolLauncher.Core;
using ToolLauncher.Network;
using ToolLauncher.Storage;
using ToolLauncher.Minecraft.Settings;

namespace ToolLauncher.Minecraft;

public sealed record InstalledGame(GameTarget Target, JsonElement Version, JsonElement Fabric, string Profile, string Natives, string Classpath, string Assets, string Java);

public sealed class MinecraftInstaller(LauncherPaths paths, RemoteClient remote, LauncherLog log)
{
    public async Task<InstalledGame> PrepareAsync(GameTarget target, LauncherSettings settings, IProgress<InstallProgress> progress, CancellationToken token)
    {
        string profile = paths.Profile(target.Minecraft); Directory.CreateDirectory(profile);
        progress.Report(new("metadata"));
        using var manifest = await remote.JsonAsync("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json", token);
        var release = manifest.RootElement.GetProperty("versions").EnumerateArray().SingleOrDefault(item => item.GetProperty("id").GetString() == target.Minecraft);
        if (release.ValueKind == JsonValueKind.Undefined) throw new InvalidOperationException("Minecraft " + target.Minecraft + " is not available in Mojang's version manifest");
        string versionPath = Path.Combine(paths.Cache, "versions", target.Minecraft + ".json");
        await remote.DownloadAsync(new(RemoteClient.Trusted(release.GetProperty("url").GetString()!), release.GetProperty("sha1").GetString(), "SHA1"), versionPath, token);
        using var versionFile = JsonDocument.Parse(await System.IO.File.ReadAllTextAsync(versionPath, token));
        JsonElement version = versionFile.RootElement.Clone();
        using var fabricFile = await remote.JsonAsync("https://meta.fabricmc.net/v2/versions/loader/" + target.Minecraft + "/" + target.Loader + "/profile/json", token);
        JsonElement fabric = fabricFile.RootElement.Clone();
        if (fabric.GetProperty("inheritsFrom").GetString() != target.Minecraft) throw new InvalidDataException("Fabric profile targets another Minecraft version");
        var libraryMap = new Dictionary<string, JsonElement>();
        foreach (var collection in new[] { version.GetProperty("libraries"), fabric.GetProperty("libraries") })
            foreach (var library in collection.EnumerateArray()) if (LaunchRules.Allowed(library)) libraryMap[LaunchRules.LibraryKey(library)] = library;
        var classpath = new ConcurrentBag<string>();
        var nativeJars = new ConcurrentBag<string>();
        int finished = 0;
        progress.Report(new("libraries", 0, libraryMap.Count));
        await Parallel.ForEachAsync(libraryMap.Values, new ParallelOptions { MaxDegreeOfParallelism = 8, CancellationToken = token }, async (library, cancellation) =>
        {
            string? jar = null;
            if (library.TryGetProperty("downloads", out var downloads))
            {
                if (downloads.TryGetProperty("artifact", out var artifact)) { jar = await LibraryAsync(artifact, cancellation); classpath.Add(jar); }
                if (library.TryGetProperty("natives", out var natives) && natives.TryGetProperty("windows", out var windows) && downloads.TryGetProperty("classifiers", out var classifiers))
                {
                    string key = windows.GetString()!.Replace("${arch}", "64");
                    if (classifiers.TryGetProperty(key, out var native)) nativeJars.Add(await LibraryAsync(native, cancellation));
                }
            }
            else
            {
                string relative = LaunchRules.ArtifactPath(library.GetProperty("name").GetString()!);
                string url = (library.TryGetProperty("url", out var repository) ? repository.GetString()! : "https://libraries.minecraft.net/").TrimEnd('/') + "/" + relative;
                jar = LauncherPaths.SafeChild(Path.Combine(paths.Cache, "libraries"), relative);
                string sha1 = library.TryGetProperty("sha1", out var hash) ? hash.GetString()! : await remote.TextAsync(url + ".sha1", cancellation);
                sha1 = sha1.Split((char[]?)null, StringSplitOptions.RemoveEmptyEntries)[0];
                if (sha1.Length != 40 || !sha1.All(Uri.IsHexDigit)) throw new InvalidDataException("Library SHA-1 is invalid");
                await remote.DownloadAsync(new(RemoteClient.Trusted(url), sha1, "SHA1"), jar, cancellation); classpath.Add(jar);
            }
            if (jar != null && library.GetProperty("name").GetString()!.Contains("natives-windows")) nativeJars.Add(jar);
            progress.Report(new("libraries", Interlocked.Increment(ref finished), libraryMap.Count));
        });
        progress.Report(new("minecraft"));
        var client = version.GetProperty("downloads").GetProperty("client");
        string minecraftJar = Path.Combine(paths.Cache, "versions", target.Minecraft + ".jar");
        await remote.DownloadAsync(File(client), minecraftJar, token); classpath.Add(minecraftJar);
        string assets = Path.Combine(paths.Cache, "assets");
        var assetIndex = version.GetProperty("assetIndex");
        string indexPath = LauncherPaths.SafeChild(Path.Combine(assets, "indexes"), assetIndex.GetProperty("id").GetString()! + ".json");
        await remote.DownloadAsync(File(assetIndex), indexPath, token);
        using (var index = JsonDocument.Parse(await System.IO.File.ReadAllTextAsync(indexPath, token)))
        {
            var objects = index.RootElement.GetProperty("objects").EnumerateObject().Select(item => item.Value).DistinctBy(item => item.GetProperty("hash").GetString()).ToArray();
            finished = 0;
            await Parallel.ForEachAsync(objects, new ParallelOptions { MaxDegreeOfParallelism = 8, CancellationToken = token }, async (item, cancellation) =>
            {
                string hash = item.GetProperty("hash").GetString()!;
                if (hash.Length != 40 || !hash.All(Uri.IsHexDigit)) throw new InvalidDataException("Invalid asset hash");
                await remote.DownloadAsync(new(RemoteClient.Trusted("https://resources.download.minecraft.net/" + hash[..2] + "/" + hash), hash, "SHA1", item.GetProperty("size").GetInt64()), Path.Combine(assets, "objects", hash[..2], hash), cancellation);
                progress.Report(new("assets", Interlocked.Increment(ref finished), objects.Length));
            });
        }
        string nativesDirectory = Path.Combine(profile, ".toollauncher", "natives");
        foreach (string native in nativeJars.Distinct()) await Task.Run(() => ArchiveFiles.Extract(native, nativesDirectory, true, token), token);
        progress.Report(new("visuals"));
        await BundledClient.InstallAsync(profile, target, token);
        if (InitialGameOptions.Apply(profile, target)) log.Info("Applied initial Minecraft options for " + target.Minecraft);
        string executable = await new JavaRuntime(paths, remote, log).ResolveAsync(target, settings, progress, token);
        string launchClasspath = LaunchClasspath.Compose(classpath);
        log.Info("Prepared Minecraft " + target.Minecraft + ", Fabric " + target.Loader + ", Java " + target.Java);
        return new(target, version, fabric, profile, nativesDirectory, launchClasspath, assets, executable);
    }
    private async Task<string> LibraryAsync(JsonElement artifact, CancellationToken token)
    {
        string destination = LauncherPaths.SafeChild(Path.Combine(paths.Cache, "libraries"), artifact.GetProperty("path").GetString()!);
        await remote.DownloadAsync(File(artifact), destination, token); return destination;
    }
    private static RemoteFile File(JsonElement artifact) => new(RemoteClient.Trusted(artifact.GetProperty("url").GetString()!), artifact.GetProperty("sha1").GetString(), "SHA1", artifact.TryGetProperty("size", out var size) ? size.GetInt64() : null);
}
