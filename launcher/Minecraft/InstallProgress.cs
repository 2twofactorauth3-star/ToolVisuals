namespace ToolLauncher.Minecraft;

public sealed record InstallProgress(string Stage, int Done = 0, int Total = 0)
{
    public double Fraction => Total == 0 ? 0 : Math.Clamp((double)Done / Total, 0, 1);
}
