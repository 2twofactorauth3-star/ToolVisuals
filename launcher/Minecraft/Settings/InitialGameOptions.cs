using System.Text;
using ToolLauncher.Storage;

namespace ToolLauncher.Minecraft.Settings;

public static class InitialGameOptions
{
    public static bool Apply(string profile, GameTarget target)
    {
        string options = Path.Combine(profile, "options.txt");
        if (File.Exists(options)) return false;
        Directory.CreateDirectory(profile);
        var defaults = new Dictionary<string, string>
        {
            ["renderDistance"] = "6",
            ["simulationDistance"] = "4",
            ["guiScale"] = "2",
            ["particles"] = "2",
            ["biomeBlendRadius"] = "0",
            ["fov"] = "0.5",
            ["bobView"] = "false",
            ["fovEffectScale"] = "0.0",
            ["screenEffectScale"] = "0.0",
            ["damageTiltStrength"] = "0.0",
            ["darknessEffectScale"] = "0.0",
            ["autoJump"] = "false",
            ["soundCategory_music"] = "0.0"
        };
        if (target.Minecraft != "1.21.4") defaults["cutoutLeaves"] = "false";
        AtomicJson.Write(Path.Combine(profile, ".toollauncher", "default-options.json"),
            new { schema = 1, minecraft = target.Minecraft });
        string temporary = options + "." + Guid.NewGuid().ToString("N") + ".tmp";
        try
        {
            File.WriteAllLines(temporary, defaults.Select(entry => entry.Key + ":" + entry.Value), new UTF8Encoding(false));
            try { File.Move(temporary, options, false); }
            catch (IOException) when (File.Exists(options)) { return false; }
            return true;
        }
        finally
        {
            if (File.Exists(temporary)) File.Delete(temporary);
        }
    }
}
