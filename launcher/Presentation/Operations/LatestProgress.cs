using System.Windows.Threading;

namespace ToolLauncher.Presentation.Operations;

public sealed class LatestProgress<T> : IProgress<T>, IDisposable
{
    private readonly object sync = new();
    private readonly DispatcherTimer timer;
    private readonly Action<T> apply;
    private T? latest;
    private bool pending;
    private bool disposed;

    public LatestProgress(Dispatcher dispatcher, Action<T> apply)
    {
        this.apply = apply;
        timer = new DispatcherTimer(TimeSpan.FromMilliseconds(120), DispatcherPriority.Background, Tick, dispatcher);
    }

    public void Report(T value)
    {
        lock (sync)
        {
            if (disposed) return;
            latest = value;
            pending = true;
        }
    }

    private void Tick(object? sender, EventArgs args)
    {
        T value;
        lock (sync)
        {
            if (disposed || !pending) return;
            value = latest!;
            latest = default;
            pending = false;
        }
        apply(value);
    }

    public void Dispose()
    {
        lock (sync) { disposed = true; pending = false; latest = default; }
        timer.Stop();
        timer.Tick -= Tick;
    }
}
