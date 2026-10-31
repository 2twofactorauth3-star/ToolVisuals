using ToolLauncher.Minecraft;
using ToolLauncher.Mods.Catalog;
using ToolLauncher.Core;
using ToolLauncher.Network;
using System.Collections.Concurrent;

namespace ToolLauncher.Mods.Sources;

public sealed class GitHubSource(RemoteClient remote) : IModSource
{
    public string Name => "GitHub";
    private readonly ConcurrentDictionary<string, (DateTimeOffset Checked, bool Allowed)> repositories = new(StringComparer.OrdinalIgnoreCase);
    public async Task<IReadOnlyList<ModProject>> SearchAsync(string query, GameTarget target, int offset, CancellationToken token)
    {
        if (offset > 0) return [];
        var projects = new List<ModProject>();
        foreach (var item in OfficialRepositories.All.Where(item => (item.Title + " " + item.Description).Contains(query, StringComparison.OrdinalIgnoreCase)))
            if (await CheckRepositoryAsync(item.Repository, false, token)) projects.Add(new(Name, item.Repository, item.Title, item.Description, item.Repository.Split('/')[0], 0, "https://github.com/" + item.Repository));
        return projects;
    }

    private async Task<bool> CheckRepositoryAsync(string repository, bool refresh, CancellationToken token)
    {
        if (!OfficialRepositories.Contains(repository)) return false;
        if (!refresh && repositories.TryGetValue(repository, out var cached) && DateTimeOffset.UtcNow - cached.Checked < TimeSpan.FromMinutes(10)) return cached.Allowed;
        using var response = await remote.JsonAsync("https://api.github.com/repos/" + repository, token);
        var info = response.RootElement;
        bool allowed = string.Equals(info.GetProperty("full_name").GetString(), repository, StringComparison.OrdinalIgnoreCase) && !info.GetProperty("archived").GetBoolean() && !info.GetProperty("disabled").GetBoolean() && info.GetProperty("stargazers_count").GetInt64() >= CatalogPolicy.MinimumGitHubStars;
        repositories[repository] = (DateTimeOffset.UtcNow, allowed);
        return allowed;
    }
    public async Task<ModRelease> ResolveAsync(ModProject project, GameTarget target, string? versionId, CancellationToken token)
    {
        if (!await CheckRepositoryAsync(project.Id, true, token)) throw new InvalidDataException("This repository is not eligible for the official source catalog");
        using var json = await remote.JsonAsync("https://api.github.com/repos/" + project.Id + "/releases?per_page=30", token);
        foreach (var release in json.RootElement.EnumerateArray().Where(item => !item.GetProperty("draft").GetBoolean()))
        {
            foreach (var file in release.GetProperty("assets").EnumerateArray())
            {
                string name = file.GetProperty("name").GetString()!;
                if (!name.EndsWith(".jar") || name.Contains("sources") || name.Contains("dev.") || !name.Contains(target.Minecraft) || name.Contains("neoforge", StringComparison.OrdinalIgnoreCase) || name.Contains("-forge", StringComparison.OrdinalIgnoreCase)) continue;
                string? digest = file.TryGetProperty("digest", out var value) ? value.GetString() : null;
                if (digest == null || !digest.StartsWith("sha256:")) continue;
                return new(project, release.GetProperty("tag_name").GetString()!, release.GetProperty("name").GetString() ?? name, name, new(RemoteClient.Trusted(file.GetProperty("browser_download_url").GetString()!), digest[7..], "SHA256", file.GetProperty("size").GetInt64()), []);
            }
        }
        throw new InvalidOperationException("No verified JAR for this version was found in the official releases. Try Modrinth, or import a file downloaded from the project's website.");
    }
}
