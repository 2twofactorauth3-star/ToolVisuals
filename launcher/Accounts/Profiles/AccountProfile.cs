using System.Text.Json.Serialization;

namespace ToolLauncher.Accounts.Profiles;

[JsonConverter(typeof(JsonStringEnumConverter<AccountKind>))]
public enum AccountKind { Local, Microsoft }

public sealed record AccountProfile(string Name, AccountKind Kind, string Uuid = "", string Source = "ToolLauncher", string RefreshToken = "")
{
    [JsonIgnore] public string Identity => Kind == AccountKind.Microsoft && Guid.TryParse(Uuid, out var uuid) ? "microsoft:" + uuid.ToString("N") : Kind.ToString().ToLowerInvariant() + ":" + Name.ToLowerInvariant();
    [JsonIgnore] public bool RequiresSignIn => Kind == AccountKind.Microsoft && string.IsNullOrEmpty(RefreshToken);
    public override string ToString() => Name;
    public static string MergeSources(string left, string right) => string.Join(" · ", (left + " · " + right).Split(" · ", StringSplitOptions.RemoveEmptyEntries).Distinct(StringComparer.OrdinalIgnoreCase));

    public AccountProfile Validate()
    {
        if (!GameAccount.ValidNickname(Name) || !Enum.IsDefined(Kind)) throw new InvalidDataException("Invalid saved Minecraft account");
        if (Uuid == null || Source == null || RefreshToken == null || Source.Length > 256 || RefreshToken.Length > 32768 || Uuid.Length > 36)
            throw new InvalidDataException("Invalid saved account metadata");
        if (Uuid.Length > 0 && !Guid.TryParse(Uuid, out _)) throw new InvalidDataException("Invalid Minecraft profile UUID");
        return this;
    }
}
