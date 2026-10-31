using Microsoft.Win32;
using ToolLauncher.Accounts.Profiles;
using ToolLauncher.Diagnostics;

namespace ToolLauncher.Accounts.Import;

public sealed record LauncherAccountFile(string Launcher, string Path, AccountKind DefaultKind, bool Sqlite = false);

public static class LauncherLocations
{
    private static readonly string[] AccountFiles = ["accounts.json", "launcher_accounts.json", "TlauncherProfiles.json", "TL_profiles.json", "profiles.json", "settings/accounts.json", "settings/game/accounts.json", "data/accounts.json", "store/accounts.json"];
    private static readonly (string Name, string[] Folders, AccountKind Kind)[] Launchers =
    [
        ("TLauncher", [".tlauncher", "TLauncher"], AccountKind.Local),
        ("Lunar Client", [".lunarclient", "Lunar Client"], AccountKind.Microsoft),
        ("KLauncher", ["KLauncher", ".klauncher"], AccountKind.Local),
        ("Legacy Launcher", ["LegacyLauncher", ".legacy-launcher"], AccountKind.Local),
        ("Prism Launcher", ["PrismLauncher"], AccountKind.Microsoft),
        ("MultiMC", ["MultiMC"], AccountKind.Microsoft),
        ("PolyMC", ["PolyMC"], AccountKind.Microsoft),
        ("ATLauncher", ["ATLauncher", ".atlauncher"], AccountKind.Microsoft),
        ("SKlauncher", [".sklauncher", "SKlauncher"], AccountKind.Local),
        ("Modrinth App", ["com.modrinth.theseus", "com.modrinth.ModrinthApp", "ModrinthApp", "Modrinth App"], AccountKind.Microsoft),
        ("GDLauncher", ["gdlauncher_next", "gdlauncher", "GDLauncher"], AccountKind.Microsoft),
        ("Feather Client", [".feather", "Feather"], AccountKind.Microsoft),
        ("Badlion Client", ["Badlion Client", "BadlionClient", ".badlionclient"], AccountKind.Microsoft),
        ("CurseForge", ["CurseForge"], AccountKind.Microsoft)
    ];

    public static IReadOnlyList<LauncherAccountFile> Find(LauncherLog log)
    {
        string roaming = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
        string local = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
        string home = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
        var files = new List<LauncherAccountFile>();
        foreach (var launcher in Launchers)
            foreach (string parent in new[] { roaming, local, home, Path.Combine(local, "Programs") })
                foreach (string folder in launcher.Folders) AddFolder(files, Path.Combine(parent, folder), launcher.Name, launcher.Kind);
        string minecraft = Path.Combine(roaming, ".minecraft");
        AddFile(files, "TLauncher", Path.Combine(minecraft, "TlauncherProfiles.json"), AccountKind.Local);
        foreach (string name in new[] { "launcher_accounts.json", "launcher_profiles.json", "launcher_profiles_microsoft_store.json" }) AddFile(files, "Minecraft Launcher", Path.Combine(minecraft, name), AccountKind.Microsoft);
        foreach (string registryPath in new[] { @"Software\Microsoft\Windows\CurrentVersion\Uninstall", @"Software\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall" })
            foreach (var hive in new[] { Registry.CurrentUser, Registry.LocalMachine })
                FindInstalled(hive, registryPath, files, log);
        return files.DistinctBy(file => file.Path, StringComparer.OrdinalIgnoreCase).ToArray();
    }

    public static IReadOnlyList<LauncherAccountFile> Folder(string folder)
    {
        if (!System.IO.Path.IsPathFullyQualified(folder) || folder.StartsWith(@"\\", StringComparison.Ordinal)) throw new InvalidDataException("Choose a local launcher folder");
        var files = new List<LauncherAccountFile>();
        var known = Launchers.FirstOrDefault(launcher => launcher.Folders.Any(name => name.Equals(System.IO.Path.GetFileName(folder), StringComparison.OrdinalIgnoreCase)));
        AddFolder(files, folder, known.Name ?? "Other launcher", known.Name == null ? AccountKind.Local : known.Kind);
        return files;
    }

    private static void AddFolder(List<LauncherAccountFile> files, string root, string launcher, AccountKind kind)
    {
        foreach (string relative in AccountFiles) AddFile(files, launcher, Path.Combine(root, relative), kind);
        if (launcher is "Modrinth App" or "Other launcher") AddFile(files, launcher, Path.Combine(root, "app.db"), AccountKind.Microsoft, true);
    }

    private static void AddFile(List<LauncherAccountFile> files, string launcher, string path, AccountKind kind, bool sqlite = false)
    {
        if (File.Exists(path) && !path.StartsWith(@"\\", StringComparison.Ordinal)) files.Add(new(launcher, System.IO.Path.GetFullPath(path), kind, sqlite));
    }

    private static void FindInstalled(RegistryKey hive, string registryPath, List<LauncherAccountFile> files, LauncherLog log)
    {
        try
        {
            using var uninstall = hive.OpenSubKey(registryPath);
            if (uninstall == null) return;
            foreach (string key in uninstall.GetSubKeyNames().Take(1024))
            {
                using var application = uninstall.OpenSubKey(key);
                if (application?.GetValue("DisplayName") is not string name || application.GetValue("InstallLocation") is not string folder) continue;
                var launcher = Launchers.FirstOrDefault(launcher => name.Contains(launcher.Name, StringComparison.OrdinalIgnoreCase) || launcher.Folders.Any(part => name.Contains(part.TrimStart('.'), StringComparison.OrdinalIgnoreCase)));
                if (launcher.Name != null && System.IO.Path.IsPathFullyQualified(folder) && !folder.StartsWith(@"\\", StringComparison.Ordinal)) AddFolder(files, folder, launcher.Name, launcher.Kind);
            }
        }
        catch (Exception error) when (error is IOException or UnauthorizedAccessException or System.Security.SecurityException)
        {
            log.Error("Discover installed launcher locations", error);
        }
    }
}
