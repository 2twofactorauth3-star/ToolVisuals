using ToolLauncher.Diagnostics;
using System.Net;
using System.Net.Http;
using System.Security.Cryptography;
using System.Text.Json;
using ToolLauncher.Core;

namespace ToolLauncher.Network;

public sealed record RemoteFile(Uri Url, string? Hash = null, string Algorithm = "SHA256", long? Bytes = null);

public sealed class RemoteClient : IDisposable
{
    private readonly HttpClient client;
    private readonly SemaphoreSlim transfers = new(8);
    private readonly LauncherLog log;
    public RemoteClient(LauncherLog log, HttpMessageHandler? handler = null)
    {
        this.log = log;
        client = new(handler ?? new HttpClientHandler { AllowAutoRedirect = false, AutomaticDecompression = DecompressionMethods.All }) { Timeout = Timeout.InfiniteTimeSpan };
        client.DefaultRequestHeaders.UserAgent.ParseAdd("ToolLauncher/1.0.0 (+https://github.com/2twofactorauth3-star/ToolVisuals)");
    }
    public static Uri Trusted(string url)
    {
        var uri = new Uri(url, UriKind.Absolute);
        string[] roots = ["minecraft.net", "mojang.com", "fabricmc.net", "modrinth.com", "curseforge.com", "forgecdn.net", "github.com", "githubusercontent.com", "adoptium.net", "maven.apache.org", "minecraftservices.com", "microsoftonline.com", "live.com", "xboxlive.com"];
        if (uri.Scheme != "https" || !uri.IsDefaultPort || uri.UserInfo.Length > 0 || !roots.Any(host => uri.Host == host || uri.Host.EndsWith("." + host, StringComparison.OrdinalIgnoreCase)))
            throw new InvalidDataException("Download source is not trusted: " + uri.Host);
        return uri;
    }
    public async Task<JsonDocument> JsonAsync(string url, CancellationToken token, string? apiKey = null)
    {
        using var timeout = CancellationTokenSource.CreateLinkedTokenSource(token);
        timeout.CancelAfter(TimeSpan.FromSeconds(45));
        using var response = await SendAsync(Trusted(url), timeout.Token, apiKey);
        await using var stream = await response.Content.ReadAsStreamAsync(timeout.Token);
        using var memory = new MemoryStream();
        await CopyBoundedAsync(stream, memory, 32 * 1024 * 1024, timeout.Token);
        return JsonDocument.Parse(memory.ToArray(), new JsonDocumentOptions { MaxDepth = 96 });
    }
    public async Task<string> TextAsync(string url, CancellationToken token)
    {
        using var timeout = CancellationTokenSource.CreateLinkedTokenSource(token);
        timeout.CancelAfter(TimeSpan.FromSeconds(30));
        using var response = await SendAsync(Trusted(url), timeout.Token);
        await using var stream = await response.Content.ReadAsStreamAsync(timeout.Token);
        using var memory = new MemoryStream();
        await CopyBoundedAsync(stream, memory, 1024 * 1024, timeout.Token);
        return System.Text.Encoding.UTF8.GetString(memory.ToArray()).Trim();
    }
    public async Task DownloadAsync(RemoteFile file, string destination, CancellationToken token)
    {
        Trusted(file.Url.AbsoluteUri);
        if (File.Exists(destination) && await MatchesAsync(destination, file, token)) return;
        await transfers.WaitAsync(token);
        string temporary = destination + "." + Guid.NewGuid().ToString("N") + ".download";
        try
        {
            if (File.Exists(destination) && await MatchesAsync(destination, file, token)) return;
            using var timeout = CancellationTokenSource.CreateLinkedTokenSource(token);
            timeout.CancelAfter(TimeSpan.FromMinutes(12));
            using var response = await SendAsync(file.Url, timeout.Token);
            long limit = file.Bytes ?? 1024L * 1024 * 1024;
            if (limit is <= 0 or > 1024L * 1024 * 1024 || response.Content.Headers.ContentLength > limit) throw new InvalidDataException("Download exceeds its declared size");
            Directory.CreateDirectory(Path.GetDirectoryName(destination)!);
            await using (var output = new FileStream(temporary, FileMode.CreateNew, FileAccess.Write, FileShare.None, 131072, FileOptions.Asynchronous))
            await using (var input = await response.Content.ReadAsStreamAsync(timeout.Token))
                await CopyBoundedAsync(input, output, limit, timeout.Token);
            if (file.Hash != null && !await MatchesAsync(temporary, file, token) || file.Hash == null && (new FileInfo(temporary).Length == 0 || file.Bytes.HasValue && new FileInfo(temporary).Length != file.Bytes.Value)) throw new InvalidDataException("Download checksum or size mismatch: " + Path.GetFileName(destination));
            File.Move(temporary, destination, true);
        }
        finally { if (File.Exists(temporary)) File.Delete(temporary); transfers.Release(); }
    }
    public static async Task<bool> MatchesAsync(string path, RemoteFile file, CancellationToken token)
    {
        if (file.Bytes.HasValue && new FileInfo(path).Length != file.Bytes.Value) return false;
        if (file.Hash == null) return false;
        await using var stream = File.OpenRead(path);
        using var hash = IncrementalHash.CreateHash(new HashAlgorithmName(file.Algorithm));
        byte[] buffer = new byte[131072];
        int count;
        while ((count = await stream.ReadAsync(buffer, token)) != 0) hash.AppendData(buffer.AsSpan(0, count));
        return Convert.ToHexString(hash.GetHashAndReset()).Equals(file.Hash, StringComparison.OrdinalIgnoreCase);
    }
    private async Task<HttpResponseMessage> SendAsync(Uri uri, CancellationToken token, string? apiKey = null)
    {
        for (int redirect = 0; redirect <= 8; redirect++)
        {
            using var request = new HttpRequestMessage(HttpMethod.Get, Trusted(uri.AbsoluteUri));
            if (apiKey != null && uri.Host == "api.curseforge.com") request.Headers.Add("x-api-key", apiKey);
            var response = await client.SendAsync(request, HttpCompletionOption.ResponseHeadersRead, token);
            if ((int)response.StatusCode is >= 300 and < 400 && response.Headers.Location is { } location)
            {
                uri = location.IsAbsoluteUri ? location : new Uri(uri, location);
                response.Dispose(); continue;
            }
            if (!response.IsSuccessStatusCode)
            {
                var error = new HttpRequestException("HTTP " + (int)response.StatusCode + " from " + uri.Host, null, response.StatusCode);
                response.Dispose(); throw error;
            }
            return response;
        }
        throw new HttpRequestException("Too many download redirects");
    }
    private static async Task CopyBoundedAsync(Stream source, Stream destination, long maximum, CancellationToken token)
    {
        byte[] buffer = new byte[131072]; long total = 0; int count;
        while ((count = await source.ReadAsync(buffer, token)) != 0)
        {
            total += count;
            if (total > maximum) throw new InvalidDataException("Response exceeds its size limit");
            await destination.WriteAsync(buffer.AsMemory(0, count), token);
        }
    }
    public void Dispose() { client.Dispose(); transfers.Dispose(); }
}
