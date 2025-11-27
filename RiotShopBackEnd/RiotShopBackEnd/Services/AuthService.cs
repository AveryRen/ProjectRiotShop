using Microsoft.EntityFrameworkCore;
using RiotShopBackEnd.Data;
using RiotShopBackEnd.DTOs;
using RiotShopBackEnd.Models;

namespace RiotShopBackEnd.Services;

public class AuthService : IAuthService
{
    private readonly ApplicationDbContext _context;
    private readonly IJwtService _jwtService;
    private readonly IConfiguration _configuration;

    public AuthService(ApplicationDbContext context, IJwtService jwtService, IConfiguration configuration)
    {
        _context = context;
        _jwtService = jwtService;
        _configuration = configuration;
    }

    public async Task<ApiResponse<AuthResponse>> LoginAsync(LoginRequest request)
    {
        var user = await _context.Users
            .FirstOrDefaultAsync(u => u.Username == request.Username);

        if (user == null)
        {
            return ApiResponse<AuthResponse>.ErrorResponse("Invalid username or password");
        }

        // Verify password trực tiếp (không hash)
        if (user.PasswordHash != request.Password)
        {
            return ApiResponse<AuthResponse>.ErrorResponse("Invalid username or password");
        }

        // Update last login
        user.LastLogin = DateTime.Now;
        await _context.SaveChangesAsync();

        // Generate JWT token
        var token = _jwtService.GenerateToken(user);
        var expiryMinutes = int.Parse(_configuration["JwtSettings:ExpiryMinutes"] ?? "1440");
        var expiresAt = DateTime.UtcNow.AddMinutes(expiryMinutes);

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

        var authResponse = new AuthResponse
        {
            User = userResponse,
            Token = token,
            ExpiresAt = expiresAt
        };

        return ApiResponse<AuthResponse>.SuccessResponse(authResponse, "Login successful");
    }

    public async Task<ApiResponse<AuthResponse>> RegisterAsync(RegisterRequest request)
    {
        // Check if username exists
        if (await _context.Users.AnyAsync(u => u.Username == request.Username))
        {
            return ApiResponse<AuthResponse>.ErrorResponse("Username already exists");
        }

        // Check if email exists
        if (await _context.Users.AnyAsync(u => u.Email == request.Email))
        {
            return ApiResponse<AuthResponse>.ErrorResponse("Email already exists");
        }

        // Lưu password trực tiếp (không hash)
        var user = new User
        {
            Username = request.Username,
            PasswordHash = request.Password,
            Email = request.Email,
            FullName = request.FullName,
            PhoneNumber = request.PhoneNumber,
            IsAdmin = false,
            Balance = 0.00m,
            CreatedAt = DateTime.Now
        };

        _context.Users.Add(user);
        await _context.SaveChangesAsync();

        // Generate JWT token
        var token = _jwtService.GenerateToken(user);
        var expiryMinutes = int.Parse(_configuration["JwtSettings:ExpiryMinutes"] ?? "1440");
        var expiresAt = DateTime.UtcNow.AddMinutes(expiryMinutes);

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

        var authResponse = new AuthResponse
        {
            User = userResponse,
            Token = token,
            ExpiresAt = expiresAt
        };

        return ApiResponse<AuthResponse>.SuccessResponse(authResponse, "Registration successful");
    }
}
