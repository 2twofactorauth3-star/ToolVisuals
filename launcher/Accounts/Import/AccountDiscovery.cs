using ToolLauncher.Diagnostics;
using ToolLauncher.Storage;
using ToolLauncher.Accounts.Profiles;

namespace ToolLauncher.Accounts.Import;

public sealed class AccountDiscovery(LauncherLog log)
{
    public Task<AccountDiscoveryResult> ScanAsync(CancellationToken token) => Task.Run(() => Scan(LauncherLocations.Find(log), token), token);
    public Task<AccountDiscoveryResult> ScanFolderAsync(string folder, CancellationToken token) => Task.Run(() => Scan(LauncherLocations.Folder(folder), token), token);
    public Task<AccountDiscoveryResult> ScanFilesAsync(IReadOnlyList<LauncherAccountFile> files, CancellationToken token) => Task.Run(() => Scan(files, token), token);

    private AccountDiscoveryResult Scan(IReadOnlyList<LauncherAccountFile> files, CancellationToken token)
    {
        var accounts = new Dictionary<string, DiscoveredAccount>(StringComparer.Ordinal);
        var launchers = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
        int unreadable = 0;
        if (files.Count > 256) throw new InvalidDataException("Too many launcher account files were discovered");
        foreach (var file in files)
        {
            token.ThrowIfCancellationRequested();
            try
            {
                var info = new FileInfo(file.Path);
                if (!info.Exists) continue;
                if (info.Attributes.HasFlag(FileAttributes.ReparsePoint)) throw new InvalidDataException("Linked account files are not imported automatically");
                if (info.Length > (file.Sqlite ? 1024L * 1024 * 1024 : AtomicJson.MaximumBytes)) throw new InvalidDataException("Launcher account file is too large");
                var candidates = file.Sqlite ? ModrinthAccountReader.Read(file, token) : ReadJson(file, token);
                if (candidates.Count > 0) launchers.Add(file.Launcher);
                foreach (var candidate in candidates)
                {
                    if (accounts.TryGetValue(candidate.Identity, out var existing))
                    {
                        string sources = AccountProfile.MergeSources(existing.Source, candidate.Source);
                        accounts[candidate.Identity] = existing with { Source = sources, Preferred = existing.Preferred || candidate.Preferred };
                    }
                    else accounts.Add(candidate.Identity, candidate);
                    if (accounts.Count > AccountJsonReader.MaximumEntries) throw new InvalidDataException("Too many launcher accounts were discovered");
                }
            }
            catch (FileNotFoundException) { }
            catch (DirectoryNotFoundException) { }
            catch (Exception error) when (error is IOException or UnauthorizedAccessException or System.Text.Json.JsonException or InvalidOperationException)
            {
                unreadable++;
                log.Error("Read account metadata from " + file.Launcher, error);
            }
            if (accounts.Count > AccountJsonReader.MaximumEntries) throw new InvalidDataException("Too many launcher accounts were discovered");
        }
        log.Info("Launcher account discovery completed: sources=" + launchers.Count + ", profiles=" + accounts.Count + ", unreadable=" + unreadable);
        return new(accounts.Values.OrderByDescending(account => account.Preferred).ThenBy(account => account.Name, StringComparer.OrdinalIgnoreCase).ToArray(), launchers.Order().ToArray(), unreadable);
    }

    private static IReadOnlyList<DiscoveredAccount> ReadJson(LauncherAccountFile file, CancellationToken token)
    {
        using var stream = new FileStream(file.Path, FileMode.Open, FileAccess.Read, FileShare.ReadWrite | FileShare.Delete);
        using var bytes = new MemoryStream();
        var buffer = new byte[16384];
        int count;
        while ((count = stream.Read(buffer)) > 0)
        {
            token.ThrowIfCancellationRequested();
            if (bytes.Length + count > AtomicJson.MaximumBytes) throw new InvalidDataException("Launcher account file is too large");
            bytes.Write(buffer, 0, count);
        }
        byte[] content = bytes.ToArray();
        ReadOnlyMemory<byte> json = content.AsMemory();
        if (json.Length >= 3 && json.Span[..3].SequenceEqual(new byte[] { 0xEF, 0xBB, 0xBF })) json = json[3..];
        return AccountJsonReader.Read(json, file);
    }
}
