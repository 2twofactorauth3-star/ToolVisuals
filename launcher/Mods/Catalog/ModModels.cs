using ToolLauncher.Network;

namespace ToolLauncher.Mods.Catalog;

public sealed record ModProject(string Source, string Id, string Title, string Description, string Author, long Downloads, string Website);
public sealed record ModDependency(string ProjectId, string? VersionId = null);
public sealed record ModRelease(ModProject Project, string Id, string Version, string Filename, RemoteFile? File, IReadOnlyList<ModDependency> Dependencies);
