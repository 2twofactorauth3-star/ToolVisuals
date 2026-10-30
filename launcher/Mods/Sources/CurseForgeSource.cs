using System.Text.Json;
using ToolLauncher.Minecraft;
using ToolLauncher.Mods.Catalog;
using ToolLauncher.Network;

namespace ToolLauncher.Mods.Sources;

public sealed class CurseForgeSource(RemoteClient remote, Func<string> key) : IModSource
{
    public string Name => "CurseForge";
    private const string Api = "https://api.curseforge.com/v1/";
    private string ApiKey() => key() is { Length: > 0 } value ? value : throw new InvalidOperationException("Add your CurseForge API key in launcher settings. You can also download the file on CurseForge and import its JAR.");
    public async Task<IReadOnlyList<ModProject>> SearchAsync(string query, GameTarget target, int offset, CancellationToken token)
    {
        using var json = await remote.JsonAsync(Api + "mods/search?gameId=432&classId=6&modLoaderType=4&gameVersion=" + target.Minecraft + "&pageSize=20&index=" + Math.Max(0, offset) + "&sortField=2&sortOrder=desc&searchFilter=" + Uri.EscapeDataString(query), token, ApiKey());
        return json.RootElement.GetProperty("data").EnumerateArray().Select(Project).ToArray();
    }
    private ModProject Project(JsonElement item) => new(Name, item.GetProperty("id").GetRawText(), item.GetProperty("name").GetString()!, item.GetProperty("summary").GetString()!, string.Join(", ", item.GetProperty("authors").EnumerateArray().Select(author => author.GetProperty("name").GetString())), item.GetProperty("downloadCount").GetInt64(), item.GetProperty("links").GetProperty("websiteUrl").GetString()!);
    public async Task<ModRelease> ResolveAsync(ModProject project, GameTarget target, string? versionId, CancellationToken token)
    {
        string apiKey = ApiKey();
        if (!long.TryParse(project.Id, out _)) throw new InvalidDataException("Invalid CurseForge project ID");
        using var metadata = await remote.JsonAsync(Api + "mods/" + project.Id, token, apiKey);
        project = Project(metadata.RootElement.GetProperty("data"));
        JsonElement file;
        if (versionId != null)
        {
            if (!long.TryParse(versionId, out _)) throw new InvalidDataException("Invalid CurseForge file ID");
            using var response = await remote.JsonAsync(Api + "mods/" + project.Id + "/files/" + versionId, token, apiKey); file = response.RootElement.GetProperty("data").Clone();
        }
        else
        {
            using var response = await remote.JsonAsync(Api + "mods/" + project.Id + "/files?modLoaderType=4&gameVersion=" + target.Minecraft + "&pageSize=50", token, apiKey);
            file = response.RootElement.GetProperty("data").EnumerateArray().FirstOrDefault(item => item.GetProperty("isAvailable").GetBoolean() && !item.GetProperty("isServerPack").GetBoolean());
            if (file.ValueKind == JsonValueKind.Undefined) throw new InvalidOperationException("No compatible Fabric file is available: " + project.Title);
            file = file.Clone();
        }
        if (!file.GetProperty("gameVersions").EnumerateArray().Any(item => item.GetString() == target.Minecraft) || !file.GetProperty("gameVersions").EnumerateArray().Any(item => item.GetString() == "Fabric")) throw new InvalidDataException("CurseForge file is incompatible with this profile");
        string? url = file.TryGetProperty("downloadUrl", out var direct) ? direct.GetString() : null;
        if (metadata.RootElement.GetProperty("data").TryGetProperty("allowModDistribution", out var distribution) && distribution.ValueKind == JsonValueKind.False) url = null;
        RemoteFile? download = null;
        if (!string.IsNullOrWhiteSpace(url))
        {
            var hashes = file.GetProperty("hashes").EnumerateArray().ToArray();
            var hash = hashes.FirstOrDefault(item => item.GetProperty("algo").GetInt32() == 1);
            if (hash.ValueKind == JsonValueKind.Undefined) hash = hashes.First();
            download = new(RemoteClient.Trusted(url), hash.GetProperty("value").GetString(), hash.GetProperty("algo").GetInt32() == 1 ? "SHA1" : "MD5", file.GetProperty("fileLength").GetInt64());
        }
        var dependencies = file.GetProperty("dependencies").EnumerateArray().Where(item => item.GetProperty("relationType").GetInt32() == 3).Select(item => new ModDependency(item.GetProperty("modId").GetRawText())).ToArray();
        return new(project, file.GetProperty("id").GetRawText(), file.GetProperty("displayName").GetString()!, file.GetProperty("fileName").GetString()!, download, dependencies);
    }
}
