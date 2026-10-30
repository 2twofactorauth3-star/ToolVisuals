using ToolLauncher.Minecraft;
using ToolLauncher.Mods.Catalog;
using ToolLauncher.Core;
using ToolLauncher.Network;

namespace ToolLauncher.Mods.Sources;

public sealed class GitHubSource(RemoteClient remote) : IModSource
{
    public string Name => "GitHub";
    private static readonly (string Repository, string Title, string Description)[] Catalog = [("isXander/Controlify", "Controlify", "Controller support from the official project"), ("CaffeineMC/sodium", "Sodium", "Official Sodium releases; included with ToolVisuals"), ("IrisShaders/Iris", "Iris", "Official Iris shader loader releases"), ("henkelmax/simple-voice-chat", "Simple Voice Chat", "Official voice chat releases; included with ToolVisuals")];
    public Task<IReadOnlyList<ModProject>> SearchAsync(string query, GameTarget target, int offset, CancellationToken token)
        => Task.FromResult<IReadOnlyList<ModProject>>(offset > 0 ? [] : Catalog.Where(item => (item.Title + " " + item.Description).Contains(query, StringComparison.OrdinalIgnoreCase)).Select(item => new ModProject(Name, item.Repository, item.Title, item.Description, item.Repository.Split('/')[0], 0, "https://github.com/" + item.Repository)).ToArray());
    public async Task<ModRelease> ResolveAsync(ModProject project, GameTarget target, string? versionId, CancellationToken token)
    {
        if (!Catalog.Any(item => item.Repository == project.Id)) throw new InvalidDataException("This repository is not in the official source catalog");
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
