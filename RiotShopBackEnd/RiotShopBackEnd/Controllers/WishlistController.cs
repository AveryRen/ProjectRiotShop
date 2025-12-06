using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using RiotShopBackEnd.Data;
using RiotShopBackEnd.DTOs;
using RiotShopBackEnd.Helpers;
using RiotShopBackEnd.Models;

namespace RiotShopBackEnd.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class WishlistController : ControllerBase
{
    private readonly ApplicationDbContext _context;

    public WishlistController(ApplicationDbContext context)
    {
        _context = context;
    }

    // GET: api/wishlist/me - User xem wishlist của mình
    [HttpGet("me")]
    public async Task<ActionResult<ApiResponse<List<object>>>> GetMyWishlist()
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<List<object>>.ErrorResponse("Unauthorized"));
        }

        return await GetUserWishlist(userId.Value);
    }

    // GET: api/wishlist/user/{userId} - User chỉ xem được của mình
    [HttpGet("user/{userId}")]
    public async Task<ActionResult<ApiResponse<List<object>>>> GetUserWishlist(int userId)
    {
        var currentUserId = UserHelper.GetUserId(User);
        var isAdmin = UserHelper.IsAdmin(User);

        // User chỉ xem được wishlist của mình, Admin xem được tất cả
        if (!isAdmin && currentUserId != userId)
        {
            return Forbid();
        }

        var wishlistItems = await _context.Wishlist
            .Include(w => w.ProductTemplate)
                .ThenInclude(pt => pt!.GameType)
            .Where(w => w.UserId == userId)
            .OrderByDescending(w => w.AddedAt)
            .ToListAsync();

        var result = wishlistItems.Select(w => new
        {
            w.WishlistId,
            w.UserId,
            w.TemplateId,
            ProductTemplate = w.ProductTemplate != null ? new
            {
                w.ProductTemplate.TemplateId,
                w.ProductTemplate.Title,
                w.ProductTemplate.Description,
                w.ProductTemplate.BasePrice,
                w.ProductTemplate.ImageUrl,
                GameName = w.ProductTemplate.GameType?.Name
            } : null,
            w.AddedAt
        }).ToList();

        return Ok(ApiResponse<List<object>>.SuccessResponse(result.Cast<object>().ToList()));
    }

    // POST: api/wishlist - User thêm vào wishlist (lấy userId từ token)
    [HttpPost]
    public async Task<ActionResult<ApiResponse<Wishlist>>> AddToWishlist([FromBody] AddWishlistRequest request)
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<Wishlist>.ErrorResponse("Unauthorized"));
        }

        if (request == null || request.TemplateId <= 0)
        {
            return BadRequest(ApiResponse<Wishlist>.ErrorResponse("Invalid request data"));
        }

        // Check if already in wishlist
        var existing = await _context.Wishlist
            .FirstOrDefaultAsync(w => w.UserId == userId.Value && w.TemplateId == request.TemplateId);

        if (existing != null)
        {
            return BadRequest(ApiResponse<Wishlist>.ErrorResponse("Item already in wishlist"));
        }

        var wishlistItem = new Wishlist
        {
            UserId = userId.Value, // Lấy từ token
            TemplateId = request.TemplateId,
            AddedAt = DateTime.Now
        };

        _context.Wishlist.Add(wishlistItem);
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<Wishlist>.SuccessResponse(wishlistItem, "Item added to wishlist"));
    }

    // DELETE: api/wishlist/{id} - User chỉ xóa được của mình
    [HttpDelete("{id}")]
    public async Task<ActionResult<ApiResponse<object>>> RemoveFromWishlist(int id)
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<object>.ErrorResponse("Unauthorized"));
        }

        var wishlistItem = await _context.Wishlist
            .FirstOrDefaultAsync(w => w.WishlistId == id && w.UserId == userId.Value);

        if (wishlistItem == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("Wishlist item not found"));
        }

        _context.Wishlist.Remove(wishlistItem);
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<object>.SuccessResponse(null, "Item removed from wishlist"));
    }
}

public class AddWishlistRequest
{
    public int TemplateId { get; set; }
}
