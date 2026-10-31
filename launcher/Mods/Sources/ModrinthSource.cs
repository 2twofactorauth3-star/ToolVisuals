using System.Text.Json;
using ToolLauncher.Minecraft;
using ToolLauncher.Mods.Catalog;
using ToolLauncher.Network;

namespace ToolLauncher.Mods.Sources;

public sealed class ModrinthSource(RemoteClient remote) : IModSource
{
    public string Name => "Modrinth";
    private const string Api = "https://api.modrinth.com/v2/";
    public async Task<IReadOnlyList<ModProject>> SearchAsync(string query, GameTarget target, int offset, CancellationToken token)
    {
        string facets = JsonSerializer.Serialize(new[] { new[] { "project_type:mod" }, new[] { "categories:fabric" }, new[] { "versions:" + target.Minecraft }, new[] { "downloads >= " + CatalogPolicy.MinimumDownloads } });
        using var json = await remote.JsonAsync(Api + "search?limit=20&offset=" + Math.Max(0, offset) + "&query=" + Uri.EscapeDataString(query) + "&facets=" + Uri.EscapeDataString(facets), token);
        return json.RootElement.GetProperty("hits").EnumerateArray().Select(hit => new ModProject(Name, hit.GetProperty("project_id").GetString()!, hit.GetProperty("title").GetString()!, hit.GetProperty("description").GetString()!, hit.GetProperty("author").GetString()!, hit.GetProperty("downloads").GetInt64(), "https://modrinth.com/mod/" + hit.GetProperty("slug").GetString())).Where(CatalogPolicy.Allows).ToArray();
    }
    public async Task<ModRelease> ResolveAsync(ModProject project, GameTarget target, string? versionId, CancellationToken token)
    {
        JsonElement version;
        if (versionId != null)
        {
            using var response = await remote.JsonAsync(Api + "version/" + Uri.EscapeDataString(versionId), token); version = response.RootElement.Clone();
        }
        else
        {
            string versions = Uri.EscapeDataString(JsonSerializer.Serialize(new[] { target.Minecraft }));
            using var response = await remote.JsonAsync(Api + "project/" + Uri.EscapeDataString(project.Id) + "/version?loaders=%5B%22fabric%22%5D&game_versions=" + versions, token);
            var choices = response.RootElement.EnumerateArray().ToArray();
            version = choices.FirstOrDefault(item => item.GetProperty("version_type").GetString() == "release");
            if (version.ValueKind == JsonValueKind.Undefined) version = choices.FirstOrDefault();
            if (version.ValueKind == JsonValueKind.Undefined) throw new InvalidOperationException("No Fabric release for Minecraft " + target.Minecraft + ": " + project.Title);
            version = version.Clone();
        }
        if (!version.GetProperty("game_versions").EnumerateArray().Any(value => value.GetString() == target.Minecraft) || !version.GetProperty("loaders").EnumerateArray().Any(value => value.GetString() == "fabric"))
            throw new InvalidDataException("The required mod release is incompatible with the selected profile");
        string projectId = version.GetProperty("project_id").GetString()!;
        if (project.Id.Length > 0 && project.Id != projectId) throw new InvalidDataException("The mod version belongs to a different project");
        using var info = await remote.JsonAsync(Api + "project/" + projectId, token);
        project = new(Name, projectId, info.RootElement.GetProperty("title").GetString()!, info.RootElement.GetProperty("description").GetString()!, "", info.RootElement.GetProperty("downloads").GetInt64(), "https://modrinth.com/mod/" + info.RootElement.GetProperty("slug").GetString());
        CatalogPolicy.EnsureAllowed(project);
        var files = version.GetProperty("files").EnumerateArray().ToArray();
        var file = files.FirstOrDefault(item => item.GetProperty("primary").GetBoolean());
        if (file.ValueKind == JsonValueKind.Undefined) file = files.First();
        var download = new RemoteFile(RemoteClient.Trusted(file.GetProperty("url").GetString()!), file.GetProperty("hashes").GetProperty("sha512").GetString(), "SHA512", file.GetProperty("size").GetInt64());
        var dependencies = version.GetProperty("dependencies").EnumerateArray().Where(item => item.GetProperty("dependency_type").GetString() == "required").Select(item => new ModDependency(item.GetProperty("project_id").GetString() ?? "", item.GetProperty("version_id").GetString())).ToArray();
        return new(project, version.GetProperty("id").GetString()!, version.GetProperty("version_number").GetString()!, file.GetProperty("filename").GetString()!, download, dependencies);
    }
}
