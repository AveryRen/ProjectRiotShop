namespace RiotShopBackEnd.DTOs;

public class AuthResponse
{
    public UserResponse User { get; set; } = null!;
    public string Token { get; set; } = string.Empty;
    public DateTime ExpiresAt { get; set; }
}
