namespace ToolLauncher.Minecraft.GarbageCollection;

public sealed record GarbageCollectionSettings
{
    public GarbageCollector Minecraft1214 { get; init; }
    public GarbageCollector Minecraft12111 { get; init; }
    public GarbageCollector Minecraft263 { get; init; }
    public GarbageCollector For(GameTarget target) => target.Minecraft switch
    {
        "1.21.4" => Minecraft1214,
        "1.21.11" => Minecraft12111,
        "26.3" => Minecraft263,
        _ => throw new InvalidDataException("Unsupported garbage collector profile")
    };
    public GarbageCollectionSettings Select(GameTarget target, GarbageCollector collector)
    {
        GarbageCollectorInfo.Find(collector);
        return target.Minecraft switch
        {
            "1.21.4" => this with { Minecraft1214 = collector },
            "1.21.11" => this with { Minecraft12111 = collector },
            "26.3" => this with { Minecraft263 = collector },
            _ => throw new InvalidDataException("Unsupported garbage collector profile")
        };
    }
    public void Validate()
    {
        foreach (var collector in new[] { Minecraft1214, Minecraft12111, Minecraft263 }) GarbageCollectorInfo.Find(collector);
    }
}
