using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;
using System.Security.Claims;

namespace RiotShopBackEnd.Attributes;

[AttributeUsage(AttributeTargets.Class | AttributeTargets.Method)]
public class AdminOnlyAttribute : AuthorizeAttribute, IAuthorizationFilter
{
    public void OnAuthorization(AuthorizationFilterContext context)
    {
        var user = context.HttpContext.User;

        if (!user.Identity?.IsAuthenticated ?? true)
        {
            context.Result = new UnauthorizedObjectResult(new
            {
                success = false,
                message = "Unauthorized. Please login first."
            });
            return;
        }

        var isAdminClaim = user.Claims.FirstOrDefault(c => c.Type == "is_admin");
        var isAdmin = isAdminClaim?.Value?.ToLower() == "true";

        if (!isAdmin)
        {
            context.Result = new ForbidResult();
            return;
        }
    }
}
