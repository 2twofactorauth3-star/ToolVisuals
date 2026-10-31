using ToolLauncher.Minecraft;
using ToolLauncher.Mods.Catalog;

namespace ToolLauncher.Mods.Sources;

public interface IModSource
{
    string Name { get; }
    bool Available => true;
    Task<IReadOnlyList<ModProject>> SearchAsync(string query, GameTarget target, int offset, CancellationToken token);
    Task<ModRelease> ResolveAsync(ModProject project, GameTarget target, string? versionId, CancellationToken token);
}
