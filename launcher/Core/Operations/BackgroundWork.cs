namespace ToolLauncher.Core.Operations;

public sealed class BackgroundWork
{
    private readonly SemaphoreSlim gate = new(1, 1);

    public Task<T> RunAsync<T>(Func<CancellationToken, Task<T>> action, CancellationToken token)
        => Task.Run(async () =>
        {
            await gate.WaitAsync(token).ConfigureAwait(false);
            try
            {
                token.ThrowIfCancellationRequested();
                return await action(token).ConfigureAwait(false);
            }
            finally { gate.Release(); }
        }, token);

    public Task RunAsync(Func<CancellationToken, Task> action, CancellationToken token)
        => RunAsync(async cancellation => { await action(cancellation).ConfigureAwait(false); return true; }, token);
}
