using ToolLauncher.Storage;
using ToolLauncher.Mods.Installation;
using System.Reflection;
using System.Security.Cryptography;
using System.Text.Json;
using ToolLauncher.Core;
using ToolLauncher.Network;

namespace ToolLauncher.Minecraft;

public sealed record ClientBuild(string Minecraft, string File, string Version, string Sha512, long Bytes);

public static class BundledClient
{
    public static IReadOnlyList<ClientBuild> Builds()
    {
        using var stream = Assembly.GetExecutingAssembly().GetManifestResourceStream("ToolLauncher.Clients.clients.json");
        return stream == null ? [] : JsonSerializer.Deserialize<ClientBuild[]>(stream, Storage.AtomicJson.Options) ?? [];
    }
    public static async Task InstallAsync(string profile, GameTarget target, CancellationToken token)
    {
        var build = Builds().SingleOrDefault(item => item.Minecraft == target.Minecraft) ?? throw new InvalidOperationException("This launcher was built without ToolVisuals for " + target.Minecraft + ". Rebuild it with launcher/build.py.");
        string mods = Path.Combine(profile, "mods"); Directory.CreateDirectory(mods);
        string destination = LauncherPaths.SafeChild(mods, build.File);
        var expected = new RemoteFile(new Uri("https://github.com/2twofactorauth3-star/ToolVisuals"), build.Sha512, "SHA512", build.Bytes);
        if (!File.Exists(destination) || !await RemoteClient.MatchesAsync(destination, expected, token))
        {
            string temporary = destination + ".tmp";
            try
            {
                await using var source = Assembly.GetExecutingAssembly().GetManifestResourceStream("ToolLauncher.Clients." + build.File) ?? throw new InvalidDataException("Bundled client archive is missing");
                await using (var output = File.Create(temporary)) await source.CopyToAsync(output, token);
                if (!await RemoteClient.MatchesAsync(temporary, expected, token)) throw new InvalidDataException("Bundled client checksum mismatch");
                foreach (string old in Directory.EnumerateFiles(mods, "*.jar"))
                {
                    if (old.Equals(destination, StringComparison.OrdinalIgnoreCase)) continue;
                    if (ToolLauncher.Mods.Installation.ModArchive.Read(old).Id == "toolvisuals")
                    {
                        string backup = Path.Combine(profile, ".toollauncher", "backups", DateTime.UtcNow.Ticks + "-" + Path.GetFileName(old));
                        Directory.CreateDirectory(Path.GetDirectoryName(backup)!); File.Move(old, backup);
                    }
                }
                File.Move(temporary, destination, true);
            }
            finally { if (File.Exists(temporary)) File.Delete(temporary); }
        }
    }
}
