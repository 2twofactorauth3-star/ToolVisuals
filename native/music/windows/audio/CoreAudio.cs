using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;

namespace ToolVisuals.Media.Audio
{
    internal static class CoreAudio
    {
        private const int MaximumDevices = 16, MaximumSessions = 256;
        internal static List<IAudioChannel> Read(Action<string, Exception> report, int ownerPid = 0, int parentPid = 0)
        {
            var result = new List<IAudioChannel>(); var owners = new Dictionary<uint, AudioOwner>();
            object enumerator = null; IDeviceCollection devices = null;
            try
            {
                enumerator = Activator.CreateInstance(Type.GetTypeFromCLSID(new Guid("BCDE0395-E52F-467C-8E3D-C4579291692E")));
                Check(((IDeviceEnumerator)enumerator).EnumAudioEndpoints(0, 1, out devices));
                uint count; Check(devices.GetCount(out count));
                if (count > MaximumDevices) throw new InvalidOperationException("Audio device limit exceeded");
                for (uint deviceIndex = 0; deviceIndex < count; deviceIndex++)
                {
                    IDevice device = null; object manager = null; ISessionEnumerator sessions = null;
                    try
                    {
                        Check(devices.Item(deviceIndex, out device)); string deviceId; Check(device.GetId(out deviceId));
                        var iid = typeof(ISessionManager).GUID; Check(device.Activate(ref iid, 23, IntPtr.Zero, out manager));
                        Check(((ISessionManager)manager).GetSessionEnumerator(out sessions)); int size; Check(sessions.GetCount(out size));
                        if (size < 0 || size > MaximumSessions || result.Count + size > MaximumSessions) throw new InvalidOperationException("Audio session limit exceeded");
                        for (int index = 0; index < size; index++)
                        {
                            ISessionControl control = null;
                            try
                            {
                                Check(sessions.GetSession(index, out control)); var details = (ISessionControl2)control;
                                int state; Check(details.GetState(out state)); if (state == 2) continue;
                                uint pid; Check(details.GetProcessId(out pid)); if (pid == 0 || pid == ownerPid || pid == parentPid) continue;
                                AudioOwner owner;
                                if (!owners.TryGetValue(pid, out owner)) { owner = ProcessIdentity.Read(pid); owners.Add(pid, owner); }
                                if (owner == null) continue;
                                string identity; Check(details.GetSessionInstanceIdentifier(out identity));
                                result.Add(new Channel(control, owner, deviceId + "\0" + identity)); control = null;
                            }
                            catch (Exception error) { report("audio session", error); }
                            finally { Release(control); }
                        }
                    }
                    catch (Exception error) { report("audio device", error); }
                    finally { Release(sessions); Release(manager); Release(device); }
                }
                return result;
            }
            catch { foreach (var channel in result) channel.Dispose(); throw; }
            finally { Release(devices); Release(enumerator); }
        }
        private static void Check(int result) { Marshal.ThrowExceptionForHR(result); }
        private static void Release(object value) { if (value != null && Marshal.IsComObject(value)) Marshal.ReleaseComObject(value); }
        private sealed class Channel : IAudioChannel
        {
            private readonly ISessionControl control;
            private readonly IVolume volume;
            private readonly AudioOwner owner;
            private readonly string identity;
            public AudioOwner Owner { get { return owner; } }
            public string Identity { get { return identity; } }
            public double Level { get { float level; Check(volume.GetMasterVolume(out level)); return level; } }
            public bool Muted { get { bool muted; Check(volume.GetMute(out muted)); return muted; } }
            internal Channel(ISessionControl control, AudioOwner owner, string identity) { this.control = control; this.volume = (IVolume)control; this.owner = owner; this.identity = identity; }
            public void SetLevel(float value) { var context = Guid.Empty; Check(volume.SetMasterVolume(value, ref context)); }
            public void SetMuted(bool value) { var context = Guid.Empty; Check(volume.SetMute(value, ref context)); }
            public void Dispose() { Release(control); }
        }
        [ComImport, Guid("A95664D2-9614-4F35-A746-DE8DB63617E6"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
        private interface IDeviceEnumerator { [PreserveSig] int EnumAudioEndpoints(int flow, uint states, out IDeviceCollection devices); }
        [ComImport, Guid("0BD7A1BE-7A1A-44DB-8397-CC5392387B5E"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
        private interface IDeviceCollection { [PreserveSig] int GetCount(out uint count); [PreserveSig] int Item(uint index, out IDevice device); }
        [ComImport, Guid("D666063F-1587-4E43-81F1-B948E807363F"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
        private interface IDevice
        {
            [PreserveSig] int Activate(ref Guid iid, uint context, IntPtr parameters, [MarshalAs(UnmanagedType.IUnknown)] out object service);
            [PreserveSig] int OpenPropertyStore(uint access, out IntPtr store);
            [PreserveSig] int GetId([MarshalAs(UnmanagedType.LPWStr)] out string id);
        }
        [ComImport, Guid("77AA99A0-1BD6-484F-8BC7-2C654C9A9B6F"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
        private interface ISessionManager
        {
            [PreserveSig] int GetAudioSessionControl(ref Guid id, uint flags, out IntPtr control);
            [PreserveSig] int GetSimpleAudioVolume(ref Guid id, uint flags, out IntPtr volume);
            [PreserveSig] int GetSessionEnumerator(out ISessionEnumerator sessions);
        }
        [ComImport, Guid("E2F5BB11-0570-40CA-ACDD-3AA01277DEE8"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
        private interface ISessionEnumerator { [PreserveSig] int GetCount(out int count); [PreserveSig] int GetSession(int index, out ISessionControl control); }
        [ComImport, Guid("F4B1A599-7266-4319-A8CA-E70ACB11E8CD"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
        private interface ISessionControl { [PreserveSig] int GetState(out int state); }
        [ComImport, Guid("BFB7FF88-7239-4FC9-8FA2-07C950BE9C6D"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
        private interface ISessionControl2
        {
            [PreserveSig] int GetState(out int state);
            [PreserveSig] int GetDisplayName([MarshalAs(UnmanagedType.LPWStr)] out string name);
            [PreserveSig] int SetDisplayName([MarshalAs(UnmanagedType.LPWStr)] string name, ref Guid context);
            [PreserveSig] int GetIconPath([MarshalAs(UnmanagedType.LPWStr)] out string path);
            [PreserveSig] int SetIconPath([MarshalAs(UnmanagedType.LPWStr)] string path, ref Guid context);
            [PreserveSig] int GetGroupingParam(out Guid group);
            [PreserveSig] int SetGroupingParam(ref Guid group, ref Guid context);
            [PreserveSig] int RegisterAudioSessionNotification(IntPtr handler);
            [PreserveSig] int UnregisterAudioSessionNotification(IntPtr handler);
            [PreserveSig] int GetSessionIdentifier([MarshalAs(UnmanagedType.LPWStr)] out string id);
            [PreserveSig] int GetSessionInstanceIdentifier([MarshalAs(UnmanagedType.LPWStr)] out string id);
            [PreserveSig] int GetProcessId(out uint pid);
        }
        [ComImport, Guid("87CE5498-68D6-44E5-9215-6DA47EF883D8"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
        private interface IVolume
        {
            [PreserveSig] int SetMasterVolume(float level, ref Guid context);
            [PreserveSig] int GetMasterVolume(out float level);
            [PreserveSig] int SetMute([MarshalAs(UnmanagedType.Bool)] bool muted, ref Guid context);
            [PreserveSig] int GetMute([MarshalAs(UnmanagedType.Bool)] out bool muted);
        }
    }
}
