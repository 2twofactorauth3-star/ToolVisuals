using ToolLauncher.Core;
using ToolLauncher.Minecraft;
using ToolLauncher.Mods.Catalog;
using ToolLauncher.Mods.Sources;
using ToolLauncher.Network;
using ToolLauncher.Storage;

namespace ToolLauncher.Mods.Installation;

internal sealed record PreparedMod(ModRelease Release, ModInfo Metadata, string Staged);

internal sealed class ModDependencyResolver(RemoteClient remote, IModSource source, GameTarget target, string stage,
    IReadOnlyDictionary<string, ModInfo> existing, IEnumerable<string> included, IProgress<InstallProgress> progress)
{
    private const int MaximumProjects = 64;
    private readonly List<PreparedMod> plan = [];
    private readonly HashSet<string> visited = [];
    private readonly HashSet<string> provided = new(included);

    public async Task<IReadOnlyList<PreparedMod>> PrepareAsync(ModProject project, CancellationToken token)
    {
        await ResolveAsync(project, null, true, token);
        return plan.AsReadOnly();
    }

    private async Task ResolveAsync(ModProject entry, string? version, bool root, CancellationToken token)
    {
        token.ThrowIfCancellationRequested();
        if (!visited.Add(entry.Id + "@" + version)) return;
        if (visited.Count > MaximumProjects) throw new InvalidDataException("Mod dependency graph exceeds 64 entries");
        progress.Report(new("mods", plan.Count, visited.Count));
        ModRelease release = await source.ResolveAsync(entry, target, version, token);
        if (release.File == null) throw new ManualDownloadRequired(release.Project.Website, "The author requires downloading this file on the project's website. Download its JAR and use Import.");
        if (Path.GetFileName(release.Filename) != release.Filename || !release.Filename.EndsWith(".jar") || release.Filename.IndexOfAny(Path.GetInvalidFileNameChars()) >= 0)
            throw new InvalidDataException("Invalid mod filename");
        string staged = LauncherPaths.SafeChild(stage, release.Project.Id.Replace('/', '_') + "-" + release.Filename);
        await remote.DownloadAsync(release.File, staged, token);
        ModInfo metadata = ModArchive.Read(staged);
        ModArchive.Validate(metadata, target);
        if (provided.Contains(metadata.Id) && !existing.ContainsKey(metadata.Id))
        {
            if (root) throw new InvalidOperationException(metadata.Name + " is already included in ToolVisuals. Use its in-game settings.");
            return;
        }
        if (metadata.Id == "toolvisuals") throw new InvalidOperationException("ToolVisuals is managed by the launcher");
        if (provided.Contains(metadata.Id) && !root || plan.Any(item => item.Metadata.Id == metadata.Id)) return;
        plan.Add(new(release, metadata, staged));
        provided.Add(metadata.Id);
        foreach (var dependency in release.Dependencies)
            await ResolveAsync(new(source.Name, dependency.ProjectId, dependency.ProjectId, "", "", 0, release.Project.Website), dependency.VersionId, false, token);
    }
}
