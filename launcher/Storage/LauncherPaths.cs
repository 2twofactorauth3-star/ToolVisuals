using ToolLauncher.Minecraft;

namespace ToolLauncher.Storage;

public sealed class LauncherPaths
{
    public string Root { get; }
    public string Profiles => Path.Combine(Root, "profiles");
    public string Cache => Path.Combine(Root, "cache");
    public string Logs => Path.Combine(Root, "logs");
    public string Settings => Path.Combine(Root, "launcher.json");
    public LauncherPaths(string? root = null)
    {
        Root = Path.GetFullPath(root ?? Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "ToolLauncher"));
        Directory.CreateDirectory(Root);
    }
    public string Profile(string version) => SafeChild(Profiles, GameTarget.Find(version).Minecraft);
    public static string SafeChild(string root, string relative)
    {
        if (string.IsNullOrWhiteSpace(relative) || Path.IsPathRooted(relative) || relative.Contains(':'))
            throw new InvalidDataException("Invalid relative file path");
        string result = Path.GetFullPath(Path.Combine(root, relative.Replace('/', Path.DirectorySeparatorChar)));
        if (!result.StartsWith(Path.GetFullPath(root).TrimEnd(Path.DirectorySeparatorChar) + Path.DirectorySeparatorChar, StringComparison.OrdinalIgnoreCase))
            throw new InvalidDataException("File path escapes its installation directory");
        return result;
    }
}
