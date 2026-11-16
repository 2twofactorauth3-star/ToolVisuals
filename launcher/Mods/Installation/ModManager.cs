using System.Text.Json;
using ToolLauncher.Diagnostics;
using ToolLauncher.Storage;
using ToolLauncher.Mods.Catalog;
using ToolLauncher.Mods.Sources;
using ToolLauncher.Core;
using ToolLauncher.Minecraft;
using ToolLauncher.Network;

namespace ToolLauncher.Mods.Installation;

public sealed class ModManager(LauncherPaths paths, RemoteClient remote, LauncherLog log)
{
    public IReadOnlyList<ModInfo> Installed(GameTarget target, CancellationToken token = default)
    {
        token.ThrowIfCancellationRequested();
        string folder = Path.Combine(paths.Profile(target.Minecraft), "mods");
        if (!Directory.Exists(folder)) return [];
        var result = new List<ModInfo>();
        foreach (string path in Directory.EnumerateFiles(folder).Where(path => path.EndsWith(".jar", StringComparison.OrdinalIgnoreCase) || path.EndsWith(".jar.disabled", StringComparison.OrdinalIgnoreCase)))
        {
            token.ThrowIfCancellationRequested();
            try { result.Add(ModArchive.Read(path)); }
            catch (Exception error) when (error is IOException or InvalidOperationException or JsonException) { log.Error("Read installed mod: " + path, error); }
        }
        return result.OrderBy(mod => mod.Id == "toolvisuals" ? 0 : 1).ThenBy(mod => mod.Name).ToArray();
    }
    public IReadOnlyList<ModProject> AvailableProjects(IEnumerable<ModProject> projects, GameTarget target, CancellationToken token = default)
    {
        var identities = new List<ModIdentity>(BundledClient.ProvidedMods(target));
        foreach (var mod in Installed(target, token))
        {
            token.ThrowIfCancellationRequested();
            try { identities.AddRange(ModArchive.ReadIdentities(mod.File)); }
            catch (Exception error) when (error is IOException or InvalidOperationException or JsonException)
            {
                log.Error("Read installed mod catalog identities: " + mod.File, error);
            }
        }
        return new InstalledProjectFilter(identities).Available(projects);
    }
    public async Task InstallAsync(IModSource source, ModProject project, GameTarget target, IProgress<InstallProgress> progress, CancellationToken token)
    {
        string profile = paths.Profile(target.Minecraft); Directory.CreateDirectory(profile);
        using var lease = ProfileLease.Acquire(profile);
        var existing = Installed(target).ToDictionary(mod => mod.Id);
        var provided = new HashSet<string>(existing.Values.Where(mod => mod.Enabled).Select(mod => mod.Id));
        foreach (var mod in existing.Values.Where(mod => mod.Id == "toolvisuals" && mod.Enabled)) provided.UnionWith(ModArchive.ProvidedIds(mod.File));
        string stage = Path.Combine(profile, ".toollauncher", "staging", Guid.NewGuid().ToString("N")); Directory.CreateDirectory(stage);
        try
        {
            var resolver = new ModDependencyResolver(remote, source, target, stage, existing, provided, progress);
            var plan = await resolver.PrepareAsync(project, token);
            token.ThrowIfCancellationRequested();
            Commit(profile, plan.Select(item => (item.Metadata.Id, item.Staged, item.Release.Filename)).ToArray(), existing);
            log.Info("Installed " + project.Title + " with " + Math.Max(0, plan.Count - 1) + " required dependencies for " + target.Minecraft);
        }
        finally { if (Directory.Exists(stage)) Directory.Delete(stage, true); }
    }
    public void Import(string path, GameTarget target)
    {
        string profile = paths.Profile(target.Minecraft); Directory.CreateDirectory(profile);
        using var lease = ProfileLease.Acquire(profile);
        ModInfo info = ModArchive.Read(path); ModArchive.Validate(info, target);
        if (info.Id == "toolvisuals") throw new InvalidOperationException("ToolVisuals is managed by the launcher");
        var existing = Installed(target).ToDictionary(mod => mod.Id);
        var bundled = existing.Values.FirstOrDefault(mod => mod.Id == "toolvisuals");
        if (bundled != null && ModArchive.ProvidedIds(bundled.File).Contains(info.Id)) throw new InvalidOperationException(info.Name + " is already included in ToolVisuals");
        string temporary = Path.Combine(profile, ".toollauncher", "import-" + Guid.NewGuid().ToString("N") + ".jar"); Directory.CreateDirectory(Path.GetDirectoryName(temporary)!);
        try { File.Copy(path, temporary); Commit(profile, [(info.Id, temporary, Path.GetFileName(path))], existing); }
        finally { if (File.Exists(temporary)) File.Delete(temporary); }
    }
    public void Toggle(ModInfo mod, GameTarget target)
    {
        if (mod.Id == "toolvisuals") throw new InvalidOperationException("ToolVisuals is managed by the launcher");
        using var lease = ProfileLease.Acquire(paths.Profile(target.Minecraft));
        File.Move(mod.File, mod.Enabled ? mod.File + ".disabled" : mod.File[..^9]);
    }
    public void Remove(ModInfo mod, GameTarget target)
    {
        if (mod.Id == "toolvisuals") throw new InvalidOperationException("ToolVisuals is managed by the launcher");
        string profile = paths.Profile(target.Minecraft);
        using var lease = ProfileLease.Acquire(profile);
        string backup = Path.Combine(profile, ".toollauncher", "removed", DateTime.UtcNow.Ticks + "-" + Path.GetFileName(mod.File)); Directory.CreateDirectory(Path.GetDirectoryName(backup)!); File.Move(mod.File, backup);
    }
    private static void Commit(string profile, IReadOnlyList<(string Id, string Staged, string Filename)> plan, IReadOnlyDictionary<string, ModInfo> existing)
    {
        string mods = Path.Combine(profile, "mods"); Directory.CreateDirectory(mods);
        string backup = Path.Combine(profile, ".toollauncher", "backups", DateTime.UtcNow.Ticks.ToString()); Directory.CreateDirectory(backup);
        var journal = new List<(string Original, string Saved)>(); var installed = new List<string>();
        try
        {
            foreach (var item in plan)
            {
                string destination = LauncherPaths.SafeChild(mods, item.Filename);
                if (existing.TryGetValue(item.Id, out var previous))
                {
                    string saved = Path.Combine(backup, Path.GetFileName(previous.File)); File.Move(previous.File, saved); journal.Add((previous.File, saved));
                }
                if (File.Exists(destination)) throw new IOException("A different mod already uses this filename: " + item.Filename);
                File.Move(item.Staged, destination); installed.Add(destination);
            }
        }
        catch
        {
            foreach (string file in installed) File.Delete(file);
            foreach (var entry in journal.AsEnumerable().Reverse()) File.Move(entry.Saved, entry.Original);
            throw;
        }
    }
}

public sealed class ManualDownloadRequired(string website, string message) : Exception(message)
{
    public string Website { get; } = website;
}
