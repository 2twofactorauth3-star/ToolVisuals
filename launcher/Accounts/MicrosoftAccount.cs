using ToolLauncher.Diagnostics;
using System.Net.Http;
using System.Net.Http.Json;
using System.Text.Json;
using ToolLauncher.Core;
using ToolLauncher.Storage;
using ToolLauncher.Accounts.Profiles;

namespace ToolLauncher.Accounts;

public sealed record DeviceCode(string Code, string VerificationUri, string Message);

public sealed class MicrosoftAccount(SettingsStore settings, LauncherLog log, AccountLibrary? accounts = null) : IDisposable
{
    private readonly HttpClient client = new(new HttpClientHandler { AllowAutoRedirect = false }) { Timeout = TimeSpan.FromSeconds(45) };
    private const string Authority = "https://login.microsoftonline.com/consumers/oauth2/v2.0/";
    private const string Scope = "XboxLive.signin offline_access";
    private string ClientId() => Guid.TryParse(settings.Current.MicrosoftClientId, out _) ? settings.Current.MicrosoftClientId : throw new InvalidOperationException("Enter your Microsoft application's client ID in settings. The application must allow personal Microsoft accounts and public client/device-code authentication.");
    public async Task<GameAccount> SignInAsync(Action<DeviceCode> display, CancellationToken token)
    {
        string clientId = ClientId();
        using var code = await FormAsync("devicecode", new() { ["client_id"] = clientId, ["scope"] = Scope }, token);
        var value = code.RootElement;
        string device = value.GetProperty("device_code").GetString()!; log.Protect(device);
        display(new(value.GetProperty("user_code").GetString()!, value.GetProperty("verification_uri").GetString()!, value.GetProperty("message").GetString()!));
        int interval = Math.Max(5, value.GetProperty("interval").GetInt32());
        DateTime deadline = DateTime.UtcNow.AddSeconds(Math.Min(1800, value.GetProperty("expires_in").GetInt32()));
        while (DateTime.UtcNow < deadline)
        {
            await Task.Delay(TimeSpan.FromSeconds(interval), token);
            using var response = await client.PostAsync(Authority + "token", new FormUrlEncodedContent(new Dictionary<string, string> { ["client_id"] = clientId, ["grant_type"] = "urn:ietf:params:oauth:grant-type:device_code", ["device_code"] = device }), token);
            using var json = await JsonDocument.ParseAsync(await response.Content.ReadAsStreamAsync(token), cancellationToken: token);
            if (response.IsSuccessStatusCode) return await CompleteAsync(json.RootElement, token);
            string error = json.RootElement.TryGetProperty("error", out var name) ? name.GetString()! : "unknown_error";
            if (error == "authorization_pending") continue;
            if (error == "slow_down") { interval = Math.Min(30, interval + 5); continue; }
            throw new InvalidOperationException("Microsoft sign-in: " + error);
        }
        throw new TimeoutException("Microsoft sign-in code expired. Try signing in again.");
    }
    public async Task<GameAccount> CurrentAsync(CancellationToken token)
    {
        if (!settings.Current.UseMicrosoft) return GameAccount.Offline(settings.Current.Nickname);
        string refresh = SecretProtection.Open(settings.Current.RefreshToken); log.Protect(refresh);
        if (refresh.Length == 0) throw new InvalidOperationException("Sign in to Microsoft again");
        using var json = await FormAsync("token", new() { ["client_id"] = ClientId(), ["grant_type"] = "refresh_token", ["refresh_token"] = refresh, ["scope"] = Scope }, token);
        return await CompleteAsync(json.RootElement, token);
    }
    private async Task<GameAccount> CompleteAsync(JsonElement oauth, CancellationToken token)
    {
        string msa = oauth.GetProperty("access_token").GetString()!; log.Protect(msa);
        using var xbox = await PostAsync("https://user.auth.xboxlive.com/user/authenticate", new { Properties = new { AuthMethod = "RPS", SiteName = "user.auth.xboxlive.com", RpsTicket = "d=" + msa }, RelyingParty = "http://auth.xboxlive.com", TokenType = "JWT" }, token);
        string xboxToken = xbox.RootElement.GetProperty("Token").GetString()!; log.Protect(xboxToken);
        using var xsts = await PostAsync("https://xsts.auth.xboxlive.com/xsts/authorize", new { Properties = new { SandboxId = "RETAIL", UserTokens = new[] { xboxToken } }, RelyingParty = "rp://api.minecraftservices.com/", TokenType = "JWT" }, token);
        string xstsToken = xsts.RootElement.GetProperty("Token").GetString()!; log.Protect(xstsToken);
        var claim = xsts.RootElement.GetProperty("DisplayClaims").GetProperty("xui")[0];
        string uhs = claim.GetProperty("uhs").GetString()!;
        using var minecraft = await PostAsync("https://api.minecraftservices.com/authentication/login_with_xbox", new { identityToken = "XBL3.0 x=" + uhs + ";" + xstsToken }, token);
        string access = minecraft.RootElement.GetProperty("access_token").GetString()!; log.Protect(access);
        using var request = new HttpRequestMessage(HttpMethod.Get, "https://api.minecraftservices.com/minecraft/profile");
        request.Headers.Authorization = new("Bearer", access);
        using var response = await client.SendAsync(request, token);
        if (!response.IsSuccessStatusCode) throw new InvalidOperationException("Minecraft profile is unavailable (HTTP " + (int)response.StatusCode + "). Check that this Microsoft account owns Java Edition and is allowed to use this application.");
        using var profile = await JsonDocument.ParseAsync(await response.Content.ReadAsStreamAsync(token), cancellationToken: token);
        string name = profile.RootElement.GetProperty("name").GetString()!;
        string id = profile.RootElement.GetProperty("id").GetString()!;
        string refresh = oauth.TryGetProperty("refresh_token", out var refreshToken) ? refreshToken.GetString()! : SecretProtection.Open(settings.Current.RefreshToken); log.Protect(refresh);
        (accounts ?? new AccountLibrary(settings)).RememberMicrosoft(name, id, SecretProtection.Seal(refresh));
        return new(name, id, access, "msa", claim.TryGetProperty("xid", out var xid) ? xid.GetString() ?? "" : "");
    }
    private async Task<JsonDocument> FormAsync(string route, Dictionary<string, string> values, CancellationToken token)
    {
        using var response = await client.PostAsync(Authority + route, new FormUrlEncodedContent(values), token);
        if (!response.IsSuccessStatusCode) throw new InvalidOperationException("Microsoft " + route + " request failed (HTTP " + (int)response.StatusCode + "). Check the application ID and its public-client authentication settings.");
        return await JsonDocument.ParseAsync(await response.Content.ReadAsStreamAsync(token), cancellationToken: token);
    }
    private async Task<JsonDocument> PostAsync(string url, object value, CancellationToken token)
    {
        using var response = await client.PostAsJsonAsync(url, value, token);
        if (!response.IsSuccessStatusCode) throw new InvalidOperationException("Account authentication failed at " + new Uri(url).Host + " (HTTP " + (int)response.StatusCode + ")");
        return await JsonDocument.ParseAsync(await response.Content.ReadAsStreamAsync(token), cancellationToken: token);
    }
    public void Dispose() => client.Dispose();
}
