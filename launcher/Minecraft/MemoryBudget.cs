using System.ComponentModel;
using System.Runtime.InteropServices;

namespace ToolLauncher.Minecraft;

public sealed record MemoryBudget(long TotalMiB)
{
    public const int MinimumMiB = 1024;
    public const int MinimumSystemMiB = 2048;
    public int MaximumMiB => (int)Math.Min(int.MaxValue, TotalMiB / 2);
    public bool Supported => TotalMiB >= MinimumSystemMiB;

    public void EnsureSupported()
    {
        if (!Supported) throw new InvalidOperationException("ToolLauncher requires at least 2 GB of installed RAM (1024 MB for Minecraft). / Для ToolLauncher нужно не менее 2 ГБ ОЗУ (1024 МБ для Minecraft).");
    }

    public int Clamp(int memoryMiB)
    {
        EnsureSupported();
        return Math.Clamp(memoryMiB, MinimumMiB, MaximumMiB);
    }

    public static MemoryBudget Detect()
    {
        if (GetPhysicallyInstalledSystemMemory(out ulong kilobytes) && kilobytes > 0)
            return new((long)(kilobytes / 1024));
        var status = new MemoryStatus { Length = (uint)Marshal.SizeOf<MemoryStatus>() };
        if (!GlobalMemoryStatusEx(ref status)) throw new Win32Exception(Marshal.GetLastWin32Error(), "Could not determine installed system memory");
        return new((long)(status.TotalPhysical / (1024 * 1024)));
    }

    [DllImport("kernel32.dll", SetLastError = true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    private static extern bool GetPhysicallyInstalledSystemMemory(out ulong totalKilobytes);

    [DllImport("kernel32.dll", SetLastError = true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    private static extern bool GlobalMemoryStatusEx(ref MemoryStatus status);

    [StructLayout(LayoutKind.Sequential)]
    private struct MemoryStatus
    {
        public uint Length;
        public uint Load;
        public ulong TotalPhysical;
        public ulong AvailablePhysical;
        public ulong TotalPageFile;
        public ulong AvailablePageFile;
        public ulong TotalVirtual;
        public ulong AvailableVirtual;
        public ulong AvailableExtendedVirtual;
    }
}
