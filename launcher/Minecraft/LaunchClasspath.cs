using System.Text;

namespace ToolLauncher.Minecraft;

public static class LaunchClasspath
{
    public static string Compose(IEnumerable<string> jars) => string.Join(Path.PathSeparator,
        jars.Select(Path.GetFullPath).Distinct(StringComparer.OrdinalIgnoreCase).Order(StringComparer.Ordinal));

    public static string WriteArguments(string profile, string classpath)
    {
        string directory = Path.Combine(profile, ".toollauncher");
        Directory.CreateDirectory(directory);
        string path = Path.Combine(directory, "classpath.args");
        string temporary = path + ".tmp";
        string relative = string.Join(Path.PathSeparator, classpath.Split(Path.PathSeparator)
            .Select(entry => Path.GetRelativePath(profile, entry)));
        File.WriteAllText(temporary, "-cp\n" + Quote(relative) + "\n", new UTF8Encoding(false));
        File.Move(temporary, path, true);
        return path;
    }

    private static string Quote(string value) => "\"" + value.Replace("\\", "\\\\").Replace("\"", "\\\"")
        .Replace("\r", "\\r").Replace("\n", "\\n").Replace("\t", "\\t").Replace("\f", "\\f") + "\"";
}
