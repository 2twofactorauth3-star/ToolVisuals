using System.Diagnostics;
using System.Text;

namespace ToolLauncher.Minecraft.Runtime;

public sealed record JavaProcessResult(int ExitCode, string Output);

public static class JavaProcess
{
    private const int MaximumOutputCharacters = 32768;
    public static ProcessStartInfo StartInfo(string executable)
    {
        var info = new ProcessStartInfo(executable) { UseShellExecute = false, CreateNoWindow = true, RedirectStandardError = true, RedirectStandardOutput = true };
        foreach (string name in new[] { "JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS" }) info.Environment.Remove(name);
        return info;
    }
    public static async Task<JavaProcessResult> RunAsync(string executable, IEnumerable<string> arguments, CancellationToken token, string? lineFilter = null)
    {
        token.ThrowIfCancellationRequested();
        if (Path.GetFileName(executable).Equals("javaw.exe", StringComparison.OrdinalIgnoreCase))
        {
            executable = Path.Combine(Path.GetDirectoryName(Path.GetFullPath(executable))!, "java.exe");
            if (!File.Exists(executable)) throw new InvalidDataException("Select java.exe to inspect this Java installation");
        }
        if (!File.Exists(executable)) throw new FileNotFoundException("Java executable does not exist", executable);
        var info = StartInfo(executable);
        foreach (string argument in arguments) info.ArgumentList.Add(argument);
        using var process = Process.Start(info) ?? throw new IOException("Could not start Java");
        using var timeout = CancellationTokenSource.CreateLinkedTokenSource(token);
        timeout.CancelAfter(TimeSpan.FromSeconds(15));
        Task<string> output = ReadAsync(process.StandardOutput, timeout.Token, lineFilter), errors = ReadAsync(process.StandardError, timeout.Token, lineFilter);
        try
        {
            var remaining = new List<Task> { output, errors, process.WaitForExitAsync(timeout.Token) };
            while (remaining.Count > 0)
            {
                Task completed = await Task.WhenAny(remaining).ConfigureAwait(false);
                await completed.ConfigureAwait(false);
                remaining.Remove(completed);
            }
            return new(process.ExitCode, await output.ConfigureAwait(false) + await errors.ConfigureAwait(false));
        }
        catch (OperationCanceledException error) when (!token.IsCancellationRequested) { throw new InvalidOperationException("Java inspection timed out", error); }
        finally
        {
            try { if (!process.HasExited) process.Kill(true); }
            catch (InvalidOperationException) when (process.HasExited) { }
            _ = Task.WhenAll(output, errors).ContinueWith(completed => { _ = completed.Exception; }, CancellationToken.None,
                TaskContinuationOptions.OnlyOnFaulted | TaskContinuationOptions.ExecuteSynchronously, TaskScheduler.Default);
        }
    }
    private static async Task<string> ReadAsync(StreamReader reader, CancellationToken token, string? lineFilter)
    {
        var result = new StringBuilder(); var buffer = new char[2048];
        int count;
        while ((count = await reader.ReadAsync(buffer, token).ConfigureAwait(false)) != 0)
        {
            if (result.Length + count > (lineFilter == null ? MaximumOutputCharacters : 262144)) throw new InvalidDataException("Java inspection output exceeds its limit");
            result.Append(buffer, 0, count);
        }
        string text = result.ToString();
        return lineFilter == null ? text : string.Join('\n', text.Split('\n').Where(line => line.Contains(lineFilter, StringComparison.Ordinal)));
    }
}
