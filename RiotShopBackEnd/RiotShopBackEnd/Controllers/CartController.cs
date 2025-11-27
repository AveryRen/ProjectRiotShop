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
public class CartController : ControllerBase
{
    private readonly ApplicationDbContext _context;

    public CartController(ApplicationDbContext context)
    {
        _context = context;
    }

    // GET: api/cart/me - Xem giỏ hàng của user hiện tại
    [HttpGet("me")]
    public async Task<ActionResult<ApiResponse<List<object>>>> GetMyCart()
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<List<object>>.ErrorResponse("Unauthorized"));
        }

        var cartItems = await _context.CartItems
            .Include(ci => ci.ProductTemplate)
                .ThenInclude(pt => pt!.GameType)
            .Where(ci => ci.UserId == userId.Value)
            .OrderByDescending(ci => ci.AddedAt)
            .ToListAsync();

        var result = cartItems.Select(ci => new
        {
            ci.CartItemId,
            ci.UserId,
            ci.TemplateId,
            ci.Quantity,
            ci.AddedAt,
            ProductTemplate = ci.ProductTemplate != null ? new
            {
                ci.ProductTemplate.TemplateId,
                ci.ProductTemplate.Title,
                ci.ProductTemplate.BasePrice,
                GameName = ci.ProductTemplate.GameType?.Name,
                Inventory = _context.InventoryPackages
                    .Where(ip => ip.TemplateId == ci.TemplateId)
                    .Select(ip => new
                    {
                        ip.QuantityAvailable,
                        ip.Price
                    }).FirstOrDefault()
            } : null
        }).ToList();

        return Ok(ApiResponse<List<object>>.SuccessResponse(result.Cast<object>().ToList()));
    }

    // POST: api/cart - Thêm vào giỏ hàng
    [HttpPost]
    public async Task<ActionResult<ApiResponse<CartItem>>> AddToCart([FromBody] AddToCartRequest request)
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<CartItem>.ErrorResponse("Unauthorized"));
        }

        if (request == null || request.TemplateId <= 0 || request.Quantity <= 0)
        {
            return BadRequest(ApiResponse<CartItem>.ErrorResponse("Invalid request data"));
        }

        // Check if product exists
        var product = await _context.ProductTemplates.FindAsync(request.TemplateId);
        if (product == null)
        {
            return NotFound(ApiResponse<CartItem>.ErrorResponse("Product not found"));
        }

        // Check if already in cart
        var existing = await _context.CartItems
            .FirstOrDefaultAsync(ci => ci.UserId == userId.Value && ci.TemplateId == request.TemplateId);

        if (existing != null)
        {
            // Update quantity
            existing.Quantity += request.Quantity;
            await _context.SaveChangesAsync();
            return Ok(ApiResponse<CartItem>.SuccessResponse(existing, "Cart item updated"));
        }

        var cartItem = new CartItem
        {
            UserId = userId.Value,
            TemplateId = request.TemplateId,
            Quantity = request.Quantity,
            AddedAt = DateTime.Now
        };

        _context.CartItems.Add(cartItem);
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<CartItem>.SuccessResponse(cartItem, "Item added to cart"));
    }

    // PUT: api/cart/{id} - Cập nhật số lượng
    [HttpPut("{id}")]
    public async Task<ActionResult<ApiResponse<CartItem>>> UpdateCartItem(int id, [FromBody] UpdateCartItemRequest request)
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<CartItem>.ErrorResponse("Unauthorized"));
        }

        if (request == null || request.Quantity <= 0)
        {
            return BadRequest(ApiResponse<CartItem>.ErrorResponse("Quantity must be greater than 0"));
        }

        var cartItem = await _context.CartItems
            .FirstOrDefaultAsync(ci => ci.CartItemId == id && ci.UserId == userId.Value);

        if (cartItem == null)
        {
            return NotFound(ApiResponse<CartItem>.ErrorResponse("Cart item not found"));
        }

        cartItem.Quantity = request.Quantity;
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<CartItem>.SuccessResponse(cartItem, "Cart item updated"));
    }

    // DELETE: api/cart/{id} - Xóa khỏi giỏ hàng
    [HttpDelete("{id}")]
    public async Task<ActionResult<ApiResponse<object>>> RemoveFromCart(int id)
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<object>.ErrorResponse("Unauthorized"));
        }

        var cartItem = await _context.CartItems
            .FirstOrDefaultAsync(ci => ci.CartItemId == id && ci.UserId == userId.Value);

        if (cartItem == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("Cart item not found"));
        }

        _context.CartItems.Remove(cartItem);
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<object>.SuccessResponse(null, "Item removed from cart"));
    }

    // DELETE: api/cart/clear - Xóa toàn bộ giỏ hàng
    [HttpDelete("clear")]
    public async Task<ActionResult<ApiResponse<object>>> ClearCart()
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<object>.ErrorResponse("Unauthorized"));
        }

        var cartItems = await _context.CartItems
            .Where(ci => ci.UserId == userId.Value)
            .ToListAsync();

        _context.CartItems.RemoveRange(cartItems);
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<object>.SuccessResponse(null, "Cart cleared"));
    }
}

public class AddToCartRequest
{
    public int TemplateId { get; set; }
    public int Quantity { get; set; } = 1;
}

public class UpdateCartItemRequest
{
    public int Quantity { get; set; }
}
