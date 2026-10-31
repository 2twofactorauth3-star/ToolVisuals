using System.Text.Json;
using ToolLauncher.Accounts.Profiles;

namespace ToolLauncher.Accounts.Import;

public static class AccountJsonReader
{
    public const int MaximumEntries = 2048;

    public static IReadOnlyList<DiscoveredAccount> Read(ReadOnlyMemory<byte> bytes, LauncherAccountFile source)
    {
        using var document = JsonDocument.Parse(bytes, new JsonDocumentOptions { MaxDepth = 24, AllowTrailingCommas = true, CommentHandling = JsonCommentHandling.Skip });
        var root = document.RootElement;
        string selected = Text(root, "selectedAccountUUID", "selectedAccount", "activeAccount", "selected");
        int selectedIndex = root.ValueKind == JsonValueKind.Object && root.TryGetProperty("active", out var active) && active.ValueKind == JsonValueKind.Number && active.TryGetInt32(out int number) ? number : -1;
        var records = new List<(string Key, JsonElement Value)>();
        if (root.ValueKind == JsonValueKind.Array) AddRecords(root, records);
        else if (root.ValueKind == JsonValueKind.Object)
        {
            foreach (string key in new[] { "accounts", "authenticationDatabase", "users" })
                if (root.TryGetProperty(key, out var entries)) AddRecords(entries, records);
            if (records.Count == 0 && root.TryGetProperty("account", out var account) && account.ValueKind == JsonValueKind.Object) records.Add(("", account));
            if (records.Count == 0 && (root.TryGetProperty("minecraftProfile", out _) || root.TryGetProperty("displayName", out _) || root.TryGetProperty("username", out _))) records.Add(("", root));
        }
        if (records.Count > MaximumEntries) throw new InvalidDataException("Launcher account list exceeds the supported size");
        var result = new List<DiscoveredAccount>();
        for (int index = 0; index < records.Count; index++)
        {
            var (key, entry) = records[index];
            if (entry.ValueKind != JsonValueKind.Object) continue;
            AccountKind kind = Kind(entry, source.DefaultKind);
            string uuid = Text(entry, "uuid", "id");
            bool preferred = index == selectedIndex || EqualId(selected, key) || EqualId(selected, uuid) || Flag(entry, "active") || Flag(entry, "selected");
            var profiles = new List<(string Key, JsonElement Value)>();
            foreach (string field in new[] { "minecraftProfile", "profile", "selectedProfile" })
                if (entry.TryGetProperty(field, out var profile) && profile.ValueKind == JsonValueKind.Object) profiles.Add(("", profile));
            if (entry.TryGetProperty("profiles", out var available))
            {
                if (available.ValueKind == JsonValueKind.Array) profiles.AddRange(available.EnumerateArray().Take(MaximumEntries + 1).Select(item => ("", item)));
                if (available.ValueKind == JsonValueKind.Object) profiles.AddRange(available.EnumerateObject().Take(MaximumEntries + 1).Select(item => (item.Name, item.Value)));
            }
            if (profiles.Count == 0) profiles.Add(("", entry));
            foreach (var (profileKey, profile) in profiles)
            {
                string name = Text(profile, "displayName", "minecraftUsername", "playerName", "name", "username", "login");
                if (!GameAccount.ValidNickname(name)) continue;
                string profileId = Text(profile, "id", "uuid");
                if (!Guid.TryParse(profileId, out var parsed) && !Guid.TryParse(profileKey, out parsed) && !Guid.TryParse(uuid, out parsed) && !Guid.TryParse(key, out parsed)) parsed = Guid.Empty;
                bool chosenProfile = EqualId(Text(entry, "activeProfile"), profileId);
                result.Add(new(name, kind, parsed == Guid.Empty ? "" : parsed.ToString("N"), source.Launcher, preferred || chosenProfile));
                if (result.Count > MaximumEntries) throw new InvalidDataException("Launcher account list exceeds the supported size");
            }
        }
        return result;
    }

    private static void AddRecords(JsonElement container, List<(string Key, JsonElement Value)> records)
    {
        if (container.ValueKind == JsonValueKind.Array)
            foreach (var item in container.EnumerateArray().Take(MaximumEntries + 1)) records.Add(("", item));
        else if (container.ValueKind == JsonValueKind.Object)
            foreach (var item in container.EnumerateObject().Take(MaximumEntries + 1)) records.Add((item.Name, item.Value));
    }

    private static string Text(JsonElement entry, params string[] keys)
    {
        if (entry.ValueKind != JsonValueKind.Object) return "";
        foreach (string key in keys)
            if (entry.TryGetProperty(key, out var value) && value.ValueKind == JsonValueKind.String && value.GetString() is { Length: > 0 and <= 128 } text) return text;
        return "";
    }

    private static bool Flag(JsonElement entry, string key) => entry.TryGetProperty(key, out var value) && value.ValueKind == JsonValueKind.True;
    private static bool EqualId(string left, string right) => left.Length > 0 && right.Length > 0 && left.Replace("-", "").Equals(right.Replace("-", ""), StringComparison.OrdinalIgnoreCase);

    private static AccountKind Kind(JsonElement entry, AccountKind fallback)
    {
        string type = Text(entry, "type", "accountType", "authType").ToLowerInvariant();
        if (type is "offline" or "local" or "free" or "tlauncher" or "ely" or "ely.by" or "ely_by") return AccountKind.Local;
        if (type is "microsoft" or "msa" or "xbox" or "mojang" or "ms") return AccountKind.Microsoft;
        return fallback;
    }
}
