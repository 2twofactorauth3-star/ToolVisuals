using ToolLauncher.Storage;
using System.IO.Compression;
using ToolLauncher.Core;

namespace ToolLauncher.Minecraft;

public static class ArchiveFiles
{
    public static void Extract(string archive, string destination, bool nativesOnly, CancellationToken token)
    {
        Directory.CreateDirectory(destination);
        using var zip = ZipFile.OpenRead(archive);
        long total = 0;
        if (zip.Entries.Count > 100000) throw new InvalidDataException("Archive contains too many files");
        foreach (var entry in zip.Entries)
        {
            token.ThrowIfCancellationRequested();
            if (entry.FullName.EndsWith('/') || entry.FullName.StartsWith("META-INF/") || nativesOnly && !entry.Name.EndsWith(".dll", StringComparison.OrdinalIgnoreCase)) continue;
            if ((entry.ExternalAttributes >> 16 & 0xF000) == 0xA000) throw new InvalidDataException("Symbolic links are not supported in runtime archives");
            total += entry.Length;
            if (entry.Length > 256L * 1024 * 1024 || total > 1536L * 1024 * 1024) throw new InvalidDataException("Archive extraction exceeds its size budget");
            string target = LauncherPaths.SafeChild(destination, nativesOnly ? entry.Name : entry.FullName);
            Directory.CreateDirectory(Path.GetDirectoryName(target)!);
            using var source = entry.Open();
            using var output = File.Create(target);
            source.CopyTo(output);
        }
    }
}
