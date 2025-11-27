using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using RiotShopBackEnd.Attributes;
using RiotShopBackEnd.Data;
using RiotShopBackEnd.DTOs;
using RiotShopBackEnd.Helpers;

namespace RiotShopBackEnd.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class UsersController : ControllerBase
{
    private readonly ApplicationDbContext _context;

    public UsersController(ApplicationDbContext context)
    {
        _context = context;
    }

    // GET: api/users/me - User xem profile của mình
    [HttpGet("me")]
    public async Task<ActionResult<ApiResponse<UserResponse>>> GetMyProfile()
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<UserResponse>.ErrorResponse("Unauthorized"));
        }

        return await GetUser(userId.Value);
    }

    // GET: api/users/{id} - User chỉ xem được của mình, Admin xem được tất cả
    [HttpGet("{id}")]
    public async Task<ActionResult<ApiResponse<UserResponse>>> GetUser(int id)
    {
        var currentUserId = UserHelper.GetUserId(User);
        var isAdmin = UserHelper.IsAdmin(User);

        // User chỉ xem được profile của mình, Admin xem được tất cả
        if (!isAdmin && currentUserId != id)
        {
            return Forbid();
        }

        var user = await _context.Users.FindAsync(id);
        if (user == null)
        {
            return NotFound(ApiResponse<UserResponse>.ErrorResponse("User not found"));
        }

        var userResponse = new UserResponse
        {
            UserId = user.UserId,
            Username = user.Username,
            Email = user.Email,
            FullName = user.FullName,
            PhoneNumber = user.PhoneNumber,
            Address = user.Address,
            IsAdmin = user.IsAdmin,
            Balance = user.Balance,
            CreatedAt = user.CreatedAt,
            AvatarUrl = user.AvatarUrl,
            LastLogin = user.LastLogin
        };

        return Ok(ApiResponse<UserResponse>.SuccessResponse(userResponse));
    }

    // PUT: api/users/me - User cập nhật profile của mình
    [HttpPut("me")]
    public async Task<ActionResult<ApiResponse<UserResponse>>> UpdateMyProfile([FromBody] UpdateUserRequest request)
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<UserResponse>.ErrorResponse("Unauthorized"));
        }

        return await UpdateUser(userId.Value, request);
    }

    // PUT: api/users/{id} - User chỉ sửa được của mình, Admin sửa được tất cả
    [HttpPut("{id}")]
    public async Task<ActionResult<ApiResponse<UserResponse>>> UpdateUser(int id, [FromBody] UpdateUserRequest request)
    {
        var currentUserId = UserHelper.GetUserId(User);
        var isAdmin = UserHelper.IsAdmin(User);

        // User chỉ sửa được profile của mình, Admin sửa được tất cả
        if (!isAdmin && currentUserId != id)
        {
            return Forbid();
        }

        var user = await _context.Users.FindAsync(id);
        if (user == null)
        {
            return NotFound(ApiResponse<UserResponse>.ErrorResponse("User not found"));
        }

        // User không thể tự đổi quyền admin (chỉ admin khác mới được)
        if (!isAdmin && request.IsAdmin.HasValue)
        {
            return Forbid();
        }

        if (!string.IsNullOrEmpty(request.Email))
        {
            // Check if email already exists for another user
            var emailExists = await _context.Users
                .AnyAsync(u => u.Email == request.Email && u.UserId != id);
            
            if (emailExists)
            {
                return BadRequest(ApiResponse<UserResponse>.ErrorResponse("Email already exists"));
            }
            user.Email = request.Email;
        }

        if (request.FullName != null) user.FullName = request.FullName;
        if (request.PhoneNumber != null) user.PhoneNumber = request.PhoneNumber;
        if (request.Address != null) user.Address = request.Address;
        if (request.AvatarUrl != null) user.AvatarUrl = request.AvatarUrl;
        if (request.IsAdmin.HasValue && isAdmin) user.IsAdmin = request.IsAdmin.Value;

        await _context.SaveChangesAsync();

        var userResponse = new UserResponse
        {
            UserId = user.UserId,
            Username = user.Username,
            Email = user.Email,
            FullName = user.FullName,
            PhoneNumber = user.PhoneNumber,
            Address = user.Address,
            IsAdmin = user.IsAdmin,
            Balance = user.Balance,
            CreatedAt = user.CreatedAt,
            AvatarUrl = user.AvatarUrl,
            LastLogin = user.LastLogin
        };

        return Ok(ApiResponse<UserResponse>.SuccessResponse(userResponse, "User updated successfully"));
    }
}

public class UpdateUserRequest
{
    public string? Email { get; set; }
    public string? FullName { get; set; }
    public string? PhoneNumber { get; set; }
    public string? Address { get; set; }
    public string? AvatarUrl { get; set; }
    public bool? IsAdmin { get; set; } // Chỉ Admin mới được set
}
