using System.Text.Json;

namespace ToolLauncher.Storage;

public static class AtomicJson
{
    public static readonly JsonSerializerOptions Options = new() { PropertyNamingPolicy = JsonNamingPolicy.CamelCase, WriteIndented = true, PropertyNameCaseInsensitive = true };
    public static T Read<T>(string path) where T : new()
    {
        if (!File.Exists(path)) return new T();
        if (new FileInfo(path).Length > 4 * 1024 * 1024) throw new InvalidDataException("Settings file is too large");
        return JsonSerializer.Deserialize<T>(File.ReadAllText(path), Options) ?? throw new InvalidDataException("Settings are empty");
    }
    public static void Write<T>(string path, T value)
    {
        Directory.CreateDirectory(Path.GetDirectoryName(path)!);
        string temporary = path + "." + Guid.NewGuid().ToString("N") + ".tmp";
        try
        {
            using (var file = new FileStream(temporary, FileMode.CreateNew, FileAccess.Write, FileShare.None, 65536, FileOptions.WriteThrough))
            {
                JsonSerializer.Serialize(file, value, Options);
                file.Flush(true);
            }
            if (File.Exists(path)) File.Replace(temporary, path, null);
            else File.Move(temporary, path);
        }
        finally { if (File.Exists(temporary)) File.Delete(temporary); }
    }
}
