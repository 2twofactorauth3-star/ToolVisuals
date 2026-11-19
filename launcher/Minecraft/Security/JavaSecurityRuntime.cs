using System.Text.RegularExpressions;
using ToolLauncher.Minecraft.Runtime;

namespace ToolLauncher.Minecraft.Security;

public static class JavaSecurityRuntime
{
    public const string Mitigation = "-XX:-UseBASE64Intrinsics";
    public static bool IsSecurityArgument(string argument) => argument.StartsWith("-XX:", StringComparison.Ordinal)
        && argument[4..].TrimStart('+', '-').Split('=', 2)[0] == "UseBASE64Intrinsics";
    public static async Task<IReadOnlyList<string>> PrepareAsync(string executable, CancellationToken token)
    {
        var information = await JavaSecurityInfo.ReadAsync(executable, token).ConfigureAwait(false);
        if (information.Patched) return [];
        string[] flags = ["-XX:+UnlockDiagnosticVMOptions", Mitigation];
        var result = await JavaProcess.RunAsync(executable, flags.Concat(["-XX:+PrintFlagsFinal", "-version"]), token, "UseBASE64Intrinsics").ConfigureAwait(false);
        if (result.ExitCode != 0 || !Regex.IsMatch(result.Output, @"(?m)^\s*bool\s+UseBASE64Intrinsics\s*=\s*false\s"))
            throw new InvalidDataException("Java security protection could not be verified. Update Java before launching Minecraft.");
        return flags;
    }
}
