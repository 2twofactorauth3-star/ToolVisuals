using ToolLauncher.Mods.Installation;

namespace ToolLauncher.Mods.Catalog;

public sealed class InstalledProjectFilter
{
    private readonly HashSet<string> names = new(StringComparer.Ordinal);
    private readonly HashSet<string> links = new(StringComparer.Ordinal);

    public InstalledProjectFilter(IEnumerable<ModIdentity> installed)
    {
        foreach (var mod in installed)
        {
            names.Add(Normalize(mod.Id));
            names.Add(Normalize(mod.Name));
            foreach (string link in mod.Links)
                if (ProjectLink(link) is { Length: > 0 } identity) links.Add(identity);
        }
        names.Remove("");
    }

    public IReadOnlyList<ModProject> Available(IEnumerable<ModProject> projects) => projects.Where(project => !Contains(project)).ToArray();

    public bool Contains(ModProject project)
    {
        string link = ProjectLink(project.Website);
        return names.Contains(Normalize(project.Id)) || names.Contains(Normalize(project.Title))
            || link.Length > 0 && (links.Contains(link) || names.Contains(Normalize(link[(link.LastIndexOf('/') + 1)..])));
    }

    private static string Normalize(string value) => new(value.Where(char.IsLetterOrDigit).Select(char.ToLowerInvariant).ToArray());

    private static string ProjectLink(string value)
    {
        if (!Uri.TryCreate(value, UriKind.Absolute, out var uri) || uri.Scheme is not ("https" or "http")) return "";
        string host = uri.Host.ToLowerInvariant();
        if (host.StartsWith("www.", StringComparison.Ordinal)) host = host[4..];
        string[] path = uri.AbsolutePath.Split('/', StringSplitOptions.RemoveEmptyEntries);
        if (host == "modrinth.com" && path.Length >= 2 && path[0] is "mod" or "project") return host + "/mod/" + path[1].ToLowerInvariant();
        if (host == "curseforge.com" && path.Length >= 3 && path[0] == "minecraft" && path[1] == "mc-mods") return host + "/minecraft/mc-mods/" + path[2].ToLowerInvariant();
        if (host == "github.com" && path.Length >= 2) return host + "/" + path[0].ToLowerInvariant() + "/" + path[1].ToLowerInvariant();
        return "";
    }
}
