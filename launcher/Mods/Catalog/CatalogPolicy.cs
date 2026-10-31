namespace ToolLauncher.Mods.Catalog;

public static class CatalogPolicy
{
    public const long MinimumDownloads = 1000;
    public const int MinimumGitHubStars = 100;

    public static bool Allows(ModProject project) => project.Source switch
    {
        "Modrinth" or "CurseForge" => project.Downloads >= MinimumDownloads,
        "GitHub" => OfficialRepositories.Contains(project.Id),
        _ => false
    };

    public static void EnsureAllowed(ModProject project)
    {
        if (!Allows(project)) throw new InvalidDataException("This project is not eligible for the launcher catalog");
    }
}
