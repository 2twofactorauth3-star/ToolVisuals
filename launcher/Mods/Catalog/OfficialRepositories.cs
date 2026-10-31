namespace ToolLauncher.Mods.Catalog;

public sealed record OfficialRepository(string Repository, string Title, string Description);

public static class OfficialRepositories
{
    public static readonly IReadOnlyList<OfficialRepository> All = Array.AsReadOnly<OfficialRepository>([
        new("isXander/Controlify", "Controlify", "Controller support from the official project"),
        new("CaffeineMC/sodium", "Sodium", "Official Sodium releases; included with ToolVisuals"),
        new("IrisShaders/Iris", "Iris", "Official Iris shader loader releases"),
        new("henkelmax/simple-voice-chat", "Simple Voice Chat", "Official voice chat releases; included with ToolVisuals")
    ]);

    public static bool Contains(string repository) => All.Any(item => item.Repository.Equals(repository, StringComparison.OrdinalIgnoreCase));
}
