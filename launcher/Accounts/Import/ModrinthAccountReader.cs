using System.Diagnostics;
using System.Runtime.InteropServices;
using ToolLauncher.Accounts.Profiles;

namespace ToolLauncher.Accounts.Import;

public static class ModrinthAccountReader
{
    [UnmanagedFunctionPointer(CallingConvention.Cdecl)]
    private delegate int ProgressCallback(IntPtr data);

    public static IReadOnlyList<DiscoveredAccount> Read(LauncherAccountFile source, CancellationToken token)
    {
        IntPtr database = IntPtr.Zero;
        IntPtr statement = IntPtr.Zero;
        try
        {
            if (sqlite3_open_v2(source.Path, out database, 1, IntPtr.Zero) != 0) throw new IOException("Could not open the launcher account database read-only");
            sqlite3_busy_timeout(database, 250);
            var watch = Stopwatch.StartNew();
            ProgressCallback progress = _ => token.IsCancellationRequested || watch.Elapsed > TimeSpan.FromSeconds(3) ? 1 : 0;
            sqlite3_progress_handler(database, 1000, progress, IntPtr.Zero);
            string query = "SELECT uuid, username, active FROM minecraft_users LIMIT " + (AccountJsonReader.MaximumEntries + 1);
            if (sqlite3_prepare_v2(database, query, -1, out statement, IntPtr.Zero) != 0) throw new InvalidDataException("The launcher account database format is not supported");
            var result = new List<DiscoveredAccount>();
            int status;
            while ((status = sqlite3_step(statement)) == 100)
            {
                token.ThrowIfCancellationRequested();
                string uuid = Column(statement, 0);
                string name = Column(statement, 1);
                if (!GameAccount.ValidNickname(name)) continue;
                result.Add(new(name, AccountKind.Microsoft, Guid.TryParse(uuid, out var parsed) ? parsed.ToString("N") : "", source.Launcher, sqlite3_column_int(statement, 2) == 1));
                if (result.Count > AccountJsonReader.MaximumEntries) throw new InvalidDataException("Launcher account list exceeds the supported size");
            }
            token.ThrowIfCancellationRequested();
            if (status != 101) throw new IOException("Could not read launcher account metadata");
            GC.KeepAlive(progress);
            return result;
        }
        catch (Exception error) when (error is DllNotFoundException or EntryPointNotFoundException)
        {
            throw new IOException("This Windows installation cannot read the launcher account database", error);
        }
        finally
        {
            if (statement != IntPtr.Zero) sqlite3_finalize(statement);
            if (database != IntPtr.Zero) sqlite3_close_v2(database);
        }
    }

    private static string Column(IntPtr statement, int index)
    {
        int bytes = sqlite3_column_bytes(statement, index);
        return bytes is > 0 and <= 128 ? Marshal.PtrToStringUTF8(sqlite3_column_text(statement, index), bytes) ?? "" : "";
    }

    [DllImport("winsqlite3.dll", CallingConvention = CallingConvention.Cdecl)]
    private static extern int sqlite3_open_v2([MarshalAs(UnmanagedType.LPUTF8Str)] string filename, out IntPtr database, int flags, IntPtr vfs);
    [DllImport("winsqlite3.dll", CallingConvention = CallingConvention.Cdecl)]
    private static extern int sqlite3_close_v2(IntPtr database);
    [DllImport("winsqlite3.dll", CallingConvention = CallingConvention.Cdecl)]
    private static extern int sqlite3_prepare_v2(IntPtr database, [MarshalAs(UnmanagedType.LPUTF8Str)] string sql, int bytes, out IntPtr statement, IntPtr tail);
    [DllImport("winsqlite3.dll", CallingConvention = CallingConvention.Cdecl)]
    private static extern int sqlite3_step(IntPtr statement);
    [DllImport("winsqlite3.dll", CallingConvention = CallingConvention.Cdecl)]
    private static extern int sqlite3_finalize(IntPtr statement);
    [DllImport("winsqlite3.dll", CallingConvention = CallingConvention.Cdecl)]
    private static extern int sqlite3_column_bytes(IntPtr statement, int index);
    [DllImport("winsqlite3.dll", CallingConvention = CallingConvention.Cdecl)]
    private static extern IntPtr sqlite3_column_text(IntPtr statement, int index);
    [DllImport("winsqlite3.dll", CallingConvention = CallingConvention.Cdecl)]
    private static extern int sqlite3_column_int(IntPtr statement, int index);
    [DllImport("winsqlite3.dll", CallingConvention = CallingConvention.Cdecl)]
    private static extern int sqlite3_busy_timeout(IntPtr database, int milliseconds);
    [DllImport("winsqlite3.dll", CallingConvention = CallingConvention.Cdecl)]
    private static extern void sqlite3_progress_handler(IntPtr database, int instructions, ProgressCallback callback, IntPtr data);
}
