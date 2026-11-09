using ToolLauncher.Minecraft;
using System.IO.Compression;
using System.Text.Json;
using System.Text.RegularExpressions;
using ToolLauncher.Core;

namespace ToolLauncher.Mods.Installation;

public sealed record ModInfo(string Id, string Name, string Version, string File, bool Enabled, JsonElement Dependencies);

public static class ModArchive
{
    public static ModInfo Read(string path)
    {
        using var zip = ZipFile.OpenRead(path);
        var metadata = zip.GetEntry("fabric.mod.json") ?? throw new InvalidDataException("This file is not a Fabric mod: " + Path.GetFileName(path));
        if (metadata.Length > 256 * 1024) throw new InvalidDataException("Mod metadata is too large");
        using var file = metadata.Open();
        using var json = JsonDocument.Parse(file, new JsonDocumentOptions { MaxDepth = 48 });
        var root = json.RootElement;
        string id = ReadId(root);
        if (root.TryGetProperty("environment", out var environment) && environment.GetString() == "server") throw new InvalidDataException("This mod requires a server installation");
        return new(id, root.TryGetProperty("name", out var name) ? name.GetString() ?? id : id, root.GetProperty("version").GetString()!, path, !path.EndsWith(".disabled", StringComparison.OrdinalIgnoreCase), root.TryGetProperty("depends", out var depends) ? depends.Clone() : default);
    }
    public static HashSet<string> ProvidedIds(string archive)
    {
        return ReadIdentities(archive).Select(mod => mod.Id).ToHashSet(StringComparer.Ordinal);
    }
    public static IReadOnlyList<ModIdentity> ReadIdentities(string archive)
    {
        return ToolLauncher.Native.NativeJarScanner.ReadIdentities(archive);
    }
    public static IReadOnlyList<ModIdentity> ReadIdentities(Stream archive)
    {
        var result = new List<ModIdentity>();
        long nestedBytes = 0;
        int archives = 0;
        void Scan(Stream stream, int depth)
        {
            if (depth > 4) throw new InvalidDataException("Nested mod depth is too large");
            if (++archives > 2048) throw new InvalidDataException("Too many nested archives");
            using var zip = new ZipArchive(stream, ZipArchiveMode.Read, true);
            var metadata = zip.GetEntry("fabric.mod.json");
            if (metadata == null) return;
            if (metadata.Length > 256 * 1024) throw new InvalidDataException("Mod metadata is too large");
            using var source = metadata.Open();
            using var json = JsonDocument.Parse(source, new JsonDocumentOptions { MaxDepth = 48 });
            var root = json.RootElement;
            string id = ReadId(root);
            var links = root.TryGetProperty("contact", out var contact) && contact.ValueKind == JsonValueKind.Object
                ? contact.EnumerateObject().Where(item => item.Value.ValueKind == JsonValueKind.String).Select(item => item.Value.GetString()!).ToArray()
                : [];
            result.Add(new(id, root.TryGetProperty("name", out var name) ? name.GetString() ?? id : id, links));
            if (!json.RootElement.TryGetProperty("jars", out var jars)) return;
            if (jars.GetArrayLength() > 512) throw new InvalidDataException("Too many bundled mods");
            foreach (var nested in jars.EnumerateArray())
            {
                var entry = zip.GetEntry(nested.GetProperty("file").GetString()!) ?? throw new InvalidDataException("Bundled mod is missing");
                if (entry.Length > 128 * 1024 * 1024) throw new InvalidDataException("Nested mod is too large");
                nestedBytes += entry.Length;
                if (nestedBytes > 256 * 1024 * 1024) throw new InvalidDataException("Bundled mod data is too large");
                using var input = entry.Open(); using var memory = new MemoryStream(); input.CopyTo(memory); memory.Position = 0; Scan(memory, depth + 1);
            }
        }
        Scan(archive, 0);
        return result;
    }
    private static string ReadId(JsonElement root)
    {
        string id = root.GetProperty("id").GetString()!;
        if (!Regex.IsMatch(id, "^[a-z][a-z0-9_-]{1,63}$")) throw new InvalidDataException("Invalid Fabric mod ID");
        return id;
    }
    public static void Validate(ModInfo mod, GameTarget target)
    {
        if (mod.Dependencies.ValueKind == JsonValueKind.Object && mod.Dependencies.TryGetProperty("minecraft", out var predicate) && !Accepts(predicate, target.Minecraft))
            throw new InvalidDataException(mod.Name + " does not support Minecraft " + target.Minecraft);
    }
    private static bool Accepts(JsonElement value, string version) => value.ValueKind == JsonValueKind.Array ? value.EnumerateArray().Any(item => Accepts(item, version)) : Accepts(value.GetString() ?? "*", version);
    public static bool Accepts(string predicate, string version)
    {
        return predicate.Split("||", StringSplitOptions.TrimEntries).Any(choice => choice.Split(' ', StringSplitOptions.RemoveEmptyEntries).All(term =>
        {
            if (term == "*") return true;
            var match = Regex.Match(term, "^(>=|<=|>|<|=|~|\\^)?([0-9]+(?:\\.[0-9xX*]+)*)(?:[-+].*)?$");
            if (!match.Success) return false;
            string op = match.Groups[1].Value; string required = match.Groups[2].Value;
            string[] parts = required.Split('.'); string[] actual = version.Split('.');
            int difference = 0;
            for (int index = 0; index < Math.Max(parts.Length, actual.Length); index++)
            {
                if (index < parts.Length && parts[index] is "x" or "X" or "*") return difference == 0;
                int left = index < actual.Length ? int.Parse(actual[index]) : 0;
                int right = index < parts.Length ? int.Parse(parts[index]) : 0;
                if (difference == 0) difference = left.CompareTo(right);
            }
            return op switch { ">=" => difference >= 0, ">" => difference > 0, "<=" => difference <= 0, "<" => difference < 0, "~" => difference >= 0 && actual[0] == parts[0] && (parts.Length < 2 || actual.Length > 1 && actual[1] == parts[1]), "^" => difference >= 0 && actual[0] == parts[0], _ => difference == 0 };
        }));
    }
}
