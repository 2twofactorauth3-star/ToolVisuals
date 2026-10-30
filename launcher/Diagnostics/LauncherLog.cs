using System.Text;
using System.Text.RegularExpressions;

using ToolLauncher.Storage;

namespace ToolLauncher.Diagnostics;

public sealed class LauncherLog
{
    private readonly object gate = new();
    public string FilePath { get; }
    private readonly HashSet<string> secrets = [];
    public LauncherLog(LauncherPaths paths)
    {
        Directory.CreateDirectory(paths.Logs);
        FilePath = Path.Combine(paths.Logs, "launcher-" + DateTime.Now.ToString("yyyy-MM-dd") + ".log");
    }
    public void Protect(string? value) { if (value is { Length: >= 8 }) lock (gate) secrets.Add(value); }
    public void Info(string operation) => Write(operation);
    public void Error(string operation, Exception error) => Write(operation + Environment.NewLine + error);
    public string Redact(string value)
    {
        lock (gate) foreach (string secret in secrets) value = value.Replace(secret, "[redacted]", StringComparison.Ordinal);
        return Regex.Replace(value, "(?i)(access_token|refresh_token|authorization|x-api-key|accessToken)([\\s\"':=]+)[^\\s\",}]+", "$1$2[redacted]");
    }
    private void Write(string value)
    {
        lock (gate)
        {
            string line = DateTimeOffset.Now.ToString("O") + " " + Redact(value) + Environment.NewLine;
            try { File.AppendAllText(FilePath, line, Encoding.UTF8); }
            catch (IOException error) { System.Diagnostics.Trace.WriteLine(error); }
            catch (UnauthorizedAccessException error) { System.Diagnostics.Trace.WriteLine(error); }
        }
    }
}
