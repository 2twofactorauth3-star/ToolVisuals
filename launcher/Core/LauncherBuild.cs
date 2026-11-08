namespace ToolLauncher.Core;

public static class LauncherBuild
{
    public static readonly string Version = typeof(LauncherBuild).Assembly.GetName().Version!.ToString(3);
}
