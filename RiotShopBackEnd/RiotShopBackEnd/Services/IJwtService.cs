using RiotShopBackEnd.Models;

namespace RiotShopBackEnd.Services;

public interface IJwtService
{
    string GenerateToken(User user);
    int? ValidateToken(string token);
}
