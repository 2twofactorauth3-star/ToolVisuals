using System.Diagnostics;
using System.Text;

namespace ToolLauncher.Minecraft.Runtime;

public sealed record JavaProcessResult(int ExitCode, string Output);

public static class JavaProcess
{
    private const int MaximumOutputCharacters = 32768;
    public static async Task<JavaProcessResult> RunAsync(string executable, IEnumerable<string> arguments, CancellationToken token)
    {
        token.ThrowIfCancellationRequested();
        if (Path.GetFileName(executable).Equals("javaw.exe", StringComparison.OrdinalIgnoreCase))
        {
            executable = Path.Combine(Path.GetDirectoryName(Path.GetFullPath(executable))!, "java.exe");
            if (!File.Exists(executable)) throw new InvalidDataException("Select java.exe to inspect this Java installation");
        }
        if (!File.Exists(executable)) throw new FileNotFoundException("Java executable does not exist", executable);
        var info = new ProcessStartInfo(executable) { UseShellExecute = false, CreateNoWindow = true, RedirectStandardError = true, RedirectStandardOutput = true };
        foreach (string argument in arguments) info.ArgumentList.Add(argument);
        using var process = Process.Start(info) ?? throw new IOException("Could not start Java");
        using var timeout = CancellationTokenSource.CreateLinkedTokenSource(token);
        timeout.CancelAfter(TimeSpan.FromSeconds(15));
        Task<string> output = ReadAsync(process.StandardOutput, timeout.Token), errors = ReadAsync(process.StandardError, timeout.Token);
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
    private static async Task<string> ReadAsync(StreamReader reader, CancellationToken token)
    {
        var result = new StringBuilder(); var buffer = new char[2048];
        int count;
        while ((count = await reader.ReadAsync(buffer, token).ConfigureAwait(false)) != 0)
        {
            if (result.Length + count > MaximumOutputCharacters) throw new InvalidDataException("Java inspection output exceeds its limit");
            result.Append(buffer, 0, count);
        }
        return result.ToString();
    }
}
