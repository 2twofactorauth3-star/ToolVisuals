using System.Text.RegularExpressions;
using ToolLauncher.Minecraft.Runtime;
using RuntimeVersion = System.Version;

namespace ToolLauncher.Minecraft.Security;

public sealed record JavaSecurityInfo(string Version, string Vendor, int Major, bool Patched)
{
    public static JavaSecurityInfo Parse(string output)
    {
        string Property(string name) => Regex.Match(output, @"(?m)^\s*" + Regex.Escape(name) + @"\s*=\s*([^\r\n]+)").Groups[1].Value.Trim();
        string version = Property("java.runtime.version");
        if (version.Length == 0) version = Regex.Match(output, "(?:java|openjdk) version \\\"([^\\\"]+)\\\"").Groups[1].Value;
        string vendor = Property("java.vendor");
        return FromVersion(version, vendor);
    }
    public static JavaSecurityInfo FromVersion(string version, string vendor)
    {
        var match = Regex.Match(version, @"^(\d+)(?:\.(\d+))?(?:\.(\d+))?(?:\.(\d+))?(?:\+\d+(?:[-\w.]*)?|-LTS)?$");
        if (!match.Success) return new(version.Length == 0 ? "unknown" : version, vendor, 0, false);
        int Number(int group) => int.TryParse(match.Groups[group].Value, out int value) ? value : 0;
        var actual = new RuntimeVersion(Number(1), Number(2), Number(3), Number(4));
        RuntimeVersion? minimum = actual.Major switch
        {
            17 => new(17, 0, 21, 1), 21 => new(21, 0, 13, 1),
            25 => new(25, 0, 5, 1), 27 => new(27, 0, 1, 1), _ => null
        };
        bool knownVendor = vendor.Contains("Oracle", StringComparison.OrdinalIgnoreCase)
            || vendor.Contains("Eclipse Adoptium", StringComparison.OrdinalIgnoreCase);
        bool patched = knownVendor && minimum != null && actual >= minimum;
        if (patched && vendor.Contains("Eclipse Adoptium", StringComparison.OrdinalIgnoreCase) && actual == minimum)
        {
            var build = Regex.Match(version, @"\+(\d+)");
            patched = build.Success && int.TryParse(build.Groups[1].Value, out int number) && number >= (actual.Major == 27 ? 4 : 2);
        }
        return new(version, vendor, actual.Major, patched);
    }
    public static async Task<JavaSecurityInfo> ReadAsync(string executable, CancellationToken token)
    {
        var result = await JavaProcess.RunAsync(executable, ["-XshowSettings:properties", "-version"], token).ConfigureAwait(false);
        if (result.ExitCode != 0) throw new InvalidDataException("Could not inspect Java security: " + result.Output.Trim());
        return Parse(result.Output);
    }
}
