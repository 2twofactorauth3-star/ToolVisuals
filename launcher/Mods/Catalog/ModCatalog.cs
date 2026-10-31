using ToolLauncher.Diagnostics;
using ToolLauncher.Minecraft;
using ToolLauncher.Mods.Sources;

namespace ToolLauncher.Mods.Catalog;

public sealed record CatalogPage(IReadOnlyList<ModProject> Projects, bool HasNext);

public sealed class ModCatalog(IReadOnlyList<IModSource> sources, LauncherLog log)
{
    public const int SourcePageSize = 20;

    public async Task<CatalogPage> SearchAsync(string query, GameTarget target, int offset, CancellationToken token)
    {
        async Task<(IReadOnlyList<ModProject> Projects, bool Available, bool Failed)> Search(IModSource source)
        {
            try
            {
                if (!source.Available) return ([], false, false);
                using var deadline = CancellationTokenSource.CreateLinkedTokenSource(token);
                deadline.CancelAfter(TimeSpan.FromSeconds(12));
                var projects = await source.SearchAsync(query, target, offset, deadline.Token);
                return (projects.Where(CatalogPolicy.Allows).ToArray(), true, false);
            }
            catch (OperationCanceledException) when (token.IsCancellationRequested) { throw; }
            catch (Exception error)
            {
                log.Error("Search unified mod catalog: " + source.Name + " for " + target.Minecraft, error);
                return ([], true, true);
            }
        }
        var results = await Task.WhenAll(sources.Select(Search));
        token.ThrowIfCancellationRequested();
        if (results.Where(result => result.Available).All(result => result.Failed)) throw new IOException("The mod catalog is temporarily unavailable");
        var projects = new List<ModProject>();
        var identities = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
        for (int rank = 0; rank < results.Max(result => result.Projects.Count); rank++)
            foreach (var result in results)
                if (rank < result.Projects.Count)
                {
                    var project = result.Projects[rank];
                    string identity = Normalize(project.Title) + ":" + Normalize(project.Author);
                    if (identities.Add(identity)) projects.Add(project);
                }
        if (projects.Count == 0 && results.Any(result => result.Failed)) throw new IOException("The mod catalog is temporarily unavailable");
        return new(projects, results.Any(result => result.Projects.Count >= SourcePageSize));
    }

    private static string Normalize(string value) => new(value.Where(char.IsLetterOrDigit).Select(char.ToLowerInvariant).ToArray());
}
