using System.Security.Claims;

namespace RiotShopBackEnd.Helpers;

public static class UserHelper
{
    public static int? GetUserId(ClaimsPrincipal user)
    {
        var userIdClaim = user.Claims.FirstOrDefault(c => c.Type == ClaimTypes.NameIdentifier);
        if (userIdClaim != null && int.TryParse(userIdClaim.Value, out int userId))
        {
            return userId;
        }
        return null;
    }

    public static bool IsAdmin(ClaimsPrincipal user)
    {
        var isAdminClaim = user.Claims.FirstOrDefault(c => c.Type == "is_admin");
        return isAdminClaim?.Value?.ToLower() == "true";
    }
}
