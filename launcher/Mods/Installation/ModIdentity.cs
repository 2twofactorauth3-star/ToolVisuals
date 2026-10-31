namespace ToolLauncher.Mods.Installation;

public sealed record ModIdentity(string Id, string Name, IReadOnlyList<string> Links);
