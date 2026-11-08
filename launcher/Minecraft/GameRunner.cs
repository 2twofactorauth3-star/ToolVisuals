using ToolLauncher.Diagnostics;
using System.Diagnostics;
using System.Text.Json;
using ToolLauncher.Accounts;
using ToolLauncher.Core;
using ToolLauncher.Storage;

namespace ToolLauncher.Minecraft;

public sealed class GameRunner(LauncherPaths paths, LauncherLog log)
{
    public GameSession Start(InstalledGame game, GameAccount account, LauncherSettings settings, ProfileLease lease)
    {
        log.Protect(account.AccessToken);
        var variables = new Dictionary<string, string>
        {
            ["auth_player_name"] = account.Name, ["auth_uuid"] = account.Id, ["auth_access_token"] = account.AccessToken,
            ["auth_xuid"] = account.Xuid, ["clientid"] = settings.MicrosoftClientId, ["user_type"] = account.UserType, ["user_properties"] = "{}",
            ["version_name"] = game.Fabric.GetProperty("id").GetString()!, ["version_type"] = "release", ["game_directory"] = game.Profile,
            ["assets_root"] = game.Assets, ["assets_index_name"] = game.Version.GetProperty("assetIndex").GetProperty("id").GetString()!, ["game_assets"] = game.Assets,
            ["natives_directory"] = game.Natives, ["launcher_name"] = "ToolLauncher", ["launcher_version"] = LauncherBuild.Version, ["classpath"] = game.Classpath,
            ["classpath_separator"] = Path.PathSeparator.ToString(), ["library_directory"] = Path.Combine(paths.Cache, "libraries"), ["resolution_width"] = "1280", ["resolution_height"] = "720"
        };
        var info = new ProcessStartInfo(game.Java) { WorkingDirectory = game.Profile, UseShellExecute = false, CreateNoWindow = true, RedirectStandardOutput = true, RedirectStandardError = true };
        info.ArgumentList.Add("-Xms512M"); info.ArgumentList.Add("-Xmx" + settings.MemoryMiB + "M");
        var jvm = new List<string>();
        foreach (var metadata in new[] { game.Version, game.Fabric })
            if (metadata.TryGetProperty("arguments", out var arguments) && arguments.TryGetProperty("jvm", out var values)) jvm.AddRange(LaunchRules.Arguments(values, variables));
        for (int index = 0; index < jvm.Count; index++)
        {
            if (jvm[index] is "-cp" or "-classpath") { index++; continue; }
            if (!jvm[index].StartsWith("-Xmx") && !jvm[index].StartsWith("-Xms")) info.ArgumentList.Add(jvm[index]);
        }
        info.ArgumentList.Add("@" + LaunchClasspath.WriteArguments(game.Profile, game.Classpath));
        info.ArgumentList.Add(game.Fabric.GetProperty("mainClass").GetString()!);
        foreach (string argument in LaunchRules.Arguments(game.Version.GetProperty("arguments").GetProperty("game"), variables)) info.ArgumentList.Add(argument);
        if (game.Fabric.TryGetProperty("arguments", out var fabricArgs) && fabricArgs.TryGetProperty("game", out var extras))
            foreach (string argument in LaunchRules.Arguments(extras, variables)) info.ArgumentList.Add(argument);
        var process = Process.Start(info) ?? throw new IOException("Could not start Minecraft");
        try { lease.MarkGame(process); }
        catch { if (!process.HasExited) process.Kill(true); process.Dispose(); throw; }
        log.Info("Started Minecraft " + game.Target.Minecraft + " PID=" + process.Id + " memory=" + settings.MemoryMiB + "MiB");
        return new(process, lease, log);
    }
}

public sealed class GameSession
{
    private readonly Process process;
    private readonly ProfileLease lease;
    private readonly LauncherLog log;
    public int Pid => process.Id;
    public bool Running => !process.HasExited;
    public GameSession(Process process, ProfileLease lease, LauncherLog log) { this.process = process; this.lease = lease; this.log = log; }
    public async Task<int> CompletionAsync()
    {
        Task output = PumpAsync(process.StandardOutput); Task errors = PumpAsync(process.StandardError);
        try
        {
            await process.WaitForExitAsync(); await Task.WhenAll(output, errors);
            log.Info("Minecraft exited with code " + process.ExitCode); return process.ExitCode;
        }
        finally { lease.GameEnded(); lease.Dispose(); process.Dispose(); }
    }
    private async Task PumpAsync(StreamReader reader)
    {
        string? line;
        while ((line = await reader.ReadLineAsync()) != null) log.Info("[Minecraft] " + (line.Length > 16384 ? line[..16384] : line));
    }
}
