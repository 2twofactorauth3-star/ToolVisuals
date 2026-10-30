using System;
using System.ComponentModel;
using System.Runtime.InteropServices;
using System.Text;

namespace ToolVisuals.Media.Audio
{
    internal static class ProcessIdentity
    {
        internal static AudioOwner Read(uint pid)
        {
            IntPtr handle = OpenProcess(0x1000, false, pid);
            if (handle == IntPtr.Zero)
            {
                int error = Marshal.GetLastWin32Error();
                if (error == 87) return null;
                throw new Win32Exception(error, "Cannot read audio process identity");
            }
            try
            {
                var path = new StringBuilder(32768); int length = path.Capacity;
                if (!QueryFullProcessImageName(handle, 0, path, ref length)) throw new Win32Exception(Marshal.GetLastWin32Error(), "Cannot read audio process image");
                string image = path.ToString(), name = System.IO.Path.GetFileNameWithoutExtension(image), application = "";
                uint size = 512; var app = new StringBuilder((int)size);
                int result = GetApplicationUserModelId(handle, ref size, app);
                if (result == 0) application = app.ToString();
                else if (result != 15703 && result != 15700) throw new Win32Exception(result, "Cannot read audio application identity");
                return new AudioOwner(checked((int)pid), name, image, application);
            }
            finally { if (!CloseHandle(handle)) throw new Win32Exception(Marshal.GetLastWin32Error(), "Cannot release audio process handle"); }
        }
        [DllImport("kernel32.dll", SetLastError = true)] private static extern IntPtr OpenProcess(uint access, [MarshalAs(UnmanagedType.Bool)] bool inherit, uint pid);
        [DllImport("kernel32.dll", SetLastError = true)] [return: MarshalAs(UnmanagedType.Bool)] private static extern bool CloseHandle(IntPtr handle);
        [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)] [return: MarshalAs(UnmanagedType.Bool)] private static extern bool QueryFullProcessImageName(IntPtr process, uint flags, StringBuilder path, ref int size);
        [DllImport("kernel32.dll", CharSet = CharSet.Unicode)] private static extern int GetApplicationUserModelId(IntPtr process, ref uint size, StringBuilder application);
    }
}
