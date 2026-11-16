using System.ComponentModel;
using ToolLauncher.Minecraft.Runtime;

namespace ToolLauncher.Minecraft.GarbageCollection;

public sealed class GarbageCollectorException(GarbageCollector collector, string details, Exception? inner = null)
    : InvalidOperationException("Could not enable " + GarbageCollectorInfo.Find(collector).Name + ": " + details, inner)
{
    public readonly GarbageCollector Collector = collector;
}

public sealed record GarbageCollectorPlan(int JavaMajor, IReadOnlyList<string> Arguments);

public static class GarbageCollectorRuntime
{
    private static readonly HashSet<string> SelectionOptions = new(StringComparer.Ordinal)
    {
        "UseG1GC", "UseZGC", "UseShenandoahGC", "UseParallelGC", "UseSerialGC", "UseEpsilonGC",
        "UseConcMarkSweepGC", "UseParNewGC", "ZGenerational", "ShenandoahGCMode"
    };
    public static bool IsSelectionArgument(string argument) => argument.StartsWith("-XX:", StringComparison.Ordinal)
        && SelectionOptions.Contains(argument[4..].TrimStart('+', '-').Split('=', 2)[0]);
    public static IReadOnlyList<string> Arguments(GarbageCollector collector, int major, bool generationalShenandoah = true) => collector switch
    {
        GarbageCollector.Auto => [],
        GarbageCollector.G1 => ["-XX:+UseG1GC"],
        GarbageCollector.Zgc => major is 21 or 22 ? ["-XX:+UseZGC", "-XX:+ZGenerational"] : ["-XX:+UseZGC"],
        GarbageCollector.Shenandoah => major >= 25 && generationalShenandoah ? ["-XX:+UseShenandoahGC", "-XX:ShenandoahGCMode=generational"] : ["-XX:+UseShenandoahGC"],
        GarbageCollector.Parallel => ["-XX:+UseParallelGC"],
        _ => throw new InvalidDataException("Unknown garbage collector")
    };
    public static async Task<GarbageCollectorPlan> PrepareAsync(string executable, GarbageCollector collector, CancellationToken token)
    {
        token.ThrowIfCancellationRequested();
        GarbageCollectorInfo.Find(collector);
        if (collector == GarbageCollector.Auto) return new(0, []);
        try
        {
            int major = await JavaRuntime.MajorAsync(executable, token).ConfigureAwait(false);
            if (major < 21) throw new InvalidDataException("Java 21 or newer is required");
            var candidates = new List<IReadOnlyList<string>> { Arguments(collector, major) };
            if (collector == GarbageCollector.Shenandoah && major >= 25) candidates.Add(Arguments(collector, major, false));
            string details = "The selected Java does not provide this collector";
            foreach (var flags in candidates)
            {
                var result = await JavaProcess.RunAsync(executable, new[] { "-Xms16m", "-Xmx64m" }.Concat(flags).Concat(["-Xlog:gc", "-version"]), token).ConfigureAwait(false);
                string marker = collector switch { GarbageCollector.G1 => "Using G1", GarbageCollector.Zgc => "Using The Z Garbage Collector", GarbageCollector.Shenandoah => "Shenandoah", _ => "Using Parallel" };
                bool identified = result.Output.Split('\n').Any(line => line.Contains("Using ", StringComparison.Ordinal) && line.Contains(marker, StringComparison.Ordinal));
                if (result.ExitCode == 0 && identified) return new(major, flags);
                details = result.Output.Trim();
            }
            throw new GarbageCollectorException(collector, details);
        }
        catch (Exception error) when (error is IOException or InvalidDataException or Win32Exception || error is InvalidOperationException and not GarbageCollectorException)
        {
            throw new GarbageCollectorException(collector, error.Message, error);
        }
    }
}
