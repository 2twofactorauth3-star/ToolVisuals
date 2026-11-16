using System.Text.Json.Serialization;

namespace ToolLauncher.Minecraft.GarbageCollection;

[JsonConverter(typeof(JsonStringEnumConverter<GarbageCollector>))]
public enum GarbageCollector { Auto, G1, Zgc, Shenandoah, Parallel }

public sealed class GarbageCollectorInfo(GarbageCollector collector, string name, string descriptionKey)
{
    public readonly GarbageCollector Collector = collector;
    public readonly string Name = name;
    public readonly string DescriptionKey = descriptionKey;
    public static readonly IReadOnlyList<GarbageCollectorInfo> All = Array.AsReadOnly(new[]
    {
        new GarbageCollectorInfo(GarbageCollector.Auto, "Auto", "gcAutoDescription"),
        new GarbageCollectorInfo(GarbageCollector.G1, "G1", "gcG1Description"),
        new GarbageCollectorInfo(GarbageCollector.Zgc, "ZGC", "gcZgcDescription"),
        new GarbageCollectorInfo(GarbageCollector.Shenandoah, "Shenandoah", "gcShenandoahDescription"),
        new GarbageCollectorInfo(GarbageCollector.Parallel, "Parallel", "gcParallelDescription")
    });
    public static GarbageCollectorInfo Find(GarbageCollector collector) => All.SingleOrDefault(item => item.Collector == collector)
        ?? throw new InvalidDataException("Unknown garbage collector");
}
