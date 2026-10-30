namespace ToolLauncher.Minecraft;

public sealed class ProfileLease : IDisposable
{
    private readonly FileStream stream;
    private readonly string processFile;
    private ProfileLease(FileStream stream, string processFile) { this.stream = stream; this.processFile = processFile; }
    public static ProfileLease Acquire(string profile)
    {
        Directory.CreateDirectory(Path.Combine(profile, ".toollauncher"));
        try
        {
            var stream = new FileStream(Path.Combine(profile, ".toollauncher", "profile.lock"), FileMode.OpenOrCreate, FileAccess.ReadWrite, FileShare.None);
            string processFile = Path.Combine(profile, ".toollauncher", "game-process.json");
            try
            {
                if (File.Exists(processFile))
                {
                    var previous = Storage.AtomicJson.Read<ProcessIdentity>(processFile);
                    try
                    {
                        using var process = System.Diagnostics.Process.GetProcessById(previous.Pid);
                        if (!process.HasExited && process.StartTime.ToUniversalTime().Ticks == previous.Started)
                            throw new IOException("Minecraft is still using this profile (PID " + previous.Pid + ")");
                    }
                    catch (ArgumentException) { }
                    File.Delete(processFile);
                }
                return new(stream, processFile);
            }
            catch { stream.Dispose(); throw; }
        }
        catch (IOException error) { throw new IOException("This profile is currently running or being modified. Close Minecraft before changing its files.", error); }
    }
    public void MarkGame(System.Diagnostics.Process process) => Storage.AtomicJson.Write(processFile, new ProcessIdentity { Pid = process.Id, Started = process.StartTime.ToUniversalTime().Ticks });
    public void GameEnded() { if (File.Exists(processFile)) File.Delete(processFile); }
    public void Dispose() => stream.Dispose();
    private sealed class ProcessIdentity { public int Pid { get; set; } public long Started { get; set; } }
}
