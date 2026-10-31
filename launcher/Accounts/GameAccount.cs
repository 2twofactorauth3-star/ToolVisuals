using System.Security.Cryptography;
using System.Text;
using System.Text.RegularExpressions;

namespace ToolLauncher.Accounts;

public sealed record GameAccount(string Name, string Id, string AccessToken, string UserType, string Xuid = "")
{
    public static bool ValidNickname(string? nickname) => nickname is { Length: >= 3 and <= 16 } && Regex.IsMatch(nickname, "^[A-Za-z0-9_]+$");

    public static GameAccount Offline(string nickname)
    {
        if (!ValidNickname(nickname)) throw new InvalidOperationException("Nickname must contain 3–16 Latin letters, digits or underscores");
        byte[] hash = MD5.HashData(Encoding.UTF8.GetBytes("OfflinePlayer:" + nickname));
        hash[6] = (byte)(hash[6] & 0x0F | 0x30); hash[8] = (byte)(hash[8] & 0x3F | 0x80);
        return new(nickname, Convert.ToHexString(hash).ToLowerInvariant(), "0", "legacy");
    }
}
