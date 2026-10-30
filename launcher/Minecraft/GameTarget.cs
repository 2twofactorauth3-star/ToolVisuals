namespace ToolLauncher.Minecraft;

public sealed record GameTarget(string Minecraft, int Java, string Loader, string Description)
{
    public static readonly GameTarget[] All = [new("26.3", 25, "0.19.5", "Latest generation"), new("1.21.11", 21, "0.19.5", "Modern classic"), new("1.21.4", 21, "0.19.3", "Community favourite")];
    public static GameTarget Find(string version) => All.SingleOrDefault(target => target.Minecraft == version) ?? throw new InvalidDataException("Unsupported Minecraft version");
}
