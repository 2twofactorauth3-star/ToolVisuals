using System.Runtime.InteropServices;
using System.Text;
using System.Text.Json;
using ToolLauncher.Mods.Installation;

namespace ToolLauncher.Native;

public static class NativeJarScanner
{
    private static readonly Lazy<bool> Compatible = new(() => Abi() == 1 ? true : throw new InvalidDataException("Unsupported launcher native ABI"));
    public static void VerifyAbi() => _ = Compatible.Value;
    [DllImport("tool_launcher_core", EntryPoint = "tool_core_abi", CallingConvention = CallingConvention.Cdecl)]
    private static extern uint Abi();
    [DllImport("tool_launcher_core", EntryPoint = "tool_core_scan", CallingConvention = CallingConvention.Cdecl)]
    private static extern IntPtr Scan(byte[] path, nuint length, out nuint outputLength);
    [DllImport("tool_launcher_core", EntryPoint = "tool_core_free", CallingConvention = CallingConvention.Cdecl)]
    private static extern void Free(IntPtr pointer, nuint length);
    public static IReadOnlyList<ModIdentity> ReadIdentities(string path)
    {
        VerifyAbi();
        byte[] input = Encoding.UTF8.GetBytes(Path.GetFullPath(path));
        IntPtr pointer = Scan(input, (nuint)input.Length, out nuint length);
        if (pointer == IntPtr.Zero) throw new InvalidDataException("Native archive scanner exceeded its response budget");
        try
        {
            if (length > 4 * 1024 * 1024) throw new InvalidDataException("Invalid native response size");
            byte[] bytes = new byte[(int)length]; Marshal.Copy(pointer, bytes, 0, bytes.Length);
            using var json = JsonDocument.Parse(bytes);
            if (json.RootElement.TryGetProperty("error", out var error)) throw new InvalidDataException(error.GetString());
            return json.RootElement.GetProperty("mods").Deserialize<ModIdentity[]>(new JsonSerializerOptions(JsonSerializerDefaults.Web))
                ?? throw new InvalidDataException("Missing native scan result");
        }
        finally { Free(pointer, length); }
    }
}
