using System.Runtime.InteropServices;
using System.Text.Json;
using System.Text.RegularExpressions;

namespace ToolLauncher.Minecraft;

public static class LaunchRules
{
    public static bool Allowed(JsonElement item)
    {
        if (!item.TryGetProperty("rules", out var rules)) return true;
        bool allowed = false;
        foreach (var rule in rules.EnumerateArray())
        {
            bool matches = true;
            if (rule.TryGetProperty("features", out var features))
                matches &= features.EnumerateObject().All(feature => feature.Value.ValueKind == JsonValueKind.False);
            if (rule.TryGetProperty("os", out var os))
            {
                if (os.TryGetProperty("name", out var name)) matches &= name.GetString() == "windows";
                if (os.TryGetProperty("arch", out var arch)) matches &= arch.GetString() is "x86_64" or "amd64";
                if (os.TryGetProperty("version", out var version)) matches &= Regex.IsMatch(Environment.OSVersion.Version.ToString(), version.GetString()!, RegexOptions.CultureInvariant, TimeSpan.FromMilliseconds(100));
            }
            if (matches) allowed = rule.GetProperty("action").GetString() == "allow";
        }
        return allowed;
    }
    public static IEnumerable<string> Arguments(JsonElement arguments, IReadOnlyDictionary<string, string> variables)
    {
        foreach (var argument in arguments.EnumerateArray())
        {
            if (argument.ValueKind == JsonValueKind.String) yield return Expand(argument.GetString()!, variables);
            else if (Allowed(argument))
            {
                var value = argument.GetProperty("value");
                if (value.ValueKind == JsonValueKind.String) yield return Expand(value.GetString()!, variables);
                else foreach (var text in value.EnumerateArray()) yield return Expand(text.GetString()!, variables);
            }
        }
    }
    public static string Expand(string argument, IReadOnlyDictionary<string, string> variables) => Regex.Replace(argument, @"\$\{([^}]+)\}", match => variables.TryGetValue(match.Groups[1].Value, out string? value) ? value : throw new InvalidDataException("Unknown Minecraft launch argument: " + match.Value));
    public static string ArtifactPath(string coordinate)
    {
        string[] parts = coordinate.Split(':');
        if (parts.Length is < 3 or > 4 || parts.Any(part => !Regex.IsMatch(part, "^[a-zA-Z0-9_.+\\-]+$"))) throw new InvalidDataException("Invalid Maven library coordinate");
        return parts[0].Replace('.', '/') + "/" + parts[1] + "/" + parts[2] + "/" + parts[1] + "-" + parts[2] + (parts.Length == 4 ? "-" + parts[3] : "") + ".jar";
    }
    public static string LibraryKey(JsonElement library)
    {
        string[] name = library.GetProperty("name").GetString()!.Split(':');
        return name[0] + ":" + name[1] + (name.Length > 3 ? ":" + name[3] : "");
    }
}
