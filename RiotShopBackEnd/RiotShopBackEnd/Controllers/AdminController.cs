using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using RiotShopBackEnd.Attributes;
using RiotShopBackEnd.Data;
using RiotShopBackEnd.DTOs;
using RiotShopBackEnd.Helpers;
using RiotShopBackEnd.Models;

namespace RiotShopBackEnd.Controllers;

[ApiController]
[Route("api/admin")]
[AdminOnly]
public class AdminController : ControllerBase
{
    private readonly ApplicationDbContext _context;

    public AdminController(ApplicationDbContext context)
    {
        _context = context;
    }

    // ============================================
    // ADMIN - ORDERS
    // ============================================

    // GET: api/admin/orders - Danh sách tất cả đơn hàng
    [HttpGet("orders")]
    public async Task<ActionResult<ApiResponse<List<object>>>> GetAllOrders(
        [FromQuery] string? status,
        [FromQuery] int? userId,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 20)
    {
        var query = _context.Orders
            .Include(o => o.User)
            .Include(o => o.AccountDetail)
                .ThenInclude(ad => ad!.ProductTemplate)
            .AsQueryable();

        if (!string.IsNullOrEmpty(status))
        {
            query = query.Where(o => o.Status == status);
        }

        if (userId.HasValue)
        {
            query = query.Where(o => o.UserId == userId.Value);
        }

        var totalCount = await query.CountAsync();
        var orders = await query
            .OrderByDescending(o => o.OrderDate)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync();

        var result = orders.Select(o => new
        {
            o.OrderId,
            o.UserId,
            Username = o.User?.Username,
            o.TotalAmount,
            o.Status,
            o.OrderDate,
            o.TransactionId,
            o.PaymentMethod,
            o.RefundStatus,
            AccountDetail = o.AccountDetail != null ? new
            {
                o.AccountDetail.AccDetailId,
                ProductTemplate = o.AccountDetail.ProductTemplate != null ? new
                {
                    o.AccountDetail.ProductTemplate.TemplateId,
                    o.AccountDetail.ProductTemplate.Title
                } : null
            } : null
        }).ToList();

        return Ok(ApiResponse<List<object>>.SuccessResponse(result.Cast<object>().ToList()));
    }

    // GET: api/admin/orders/{id} - Chi tiết đơn hàng
    [HttpGet("orders/{id}")]
    public async Task<ActionResult<ApiResponse<object>>> GetOrderDetails(int id)
    {
        var order = await _context.Orders
            .Include(o => o.User)
            .Include(o => o.AccountDetail)
                .ThenInclude(ad => ad!.ProductTemplate)
                    .ThenInclude(pt => pt!.GameType)
            .FirstOrDefaultAsync(o => o.OrderId == id);

        if (order == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("Order not found"));
        }

        var result = new
        {
            order.OrderId,
            order.UserId,
            User = order.User != null ? new
            {
                order.User.Username,
                order.User.Email,
                order.User.FullName,
                order.User.PhoneNumber
            } : null,
            order.TotalAmount,
            order.Status,
            order.OrderDate,
            order.TransactionId,
            order.PaymentMethod,
            order.RefundStatus,
            AccountDetail = order.AccountDetail != null ? new
            {
                order.AccountDetail.AccDetailId,
                order.AccountDetail.AccountUsername,
                order.AccountDetail.AccountPassword,
                order.AccountDetail.RiotId,
                ProductTemplate = order.AccountDetail.ProductTemplate != null ? new
                {
                    order.AccountDetail.ProductTemplate.TemplateId,
                    order.AccountDetail.ProductTemplate.Title,
                    order.AccountDetail.ProductTemplate.BasePrice,
                    GameName = order.AccountDetail.ProductTemplate.GameType?.Name
                } : null
            } : null
        };

        return Ok(ApiResponse<object>.SuccessResponse(result));
    }

    // ============================================
    // ADMIN - PRODUCTS
    // ============================================

    // POST: api/admin/products - Tạo sản phẩm mới
    [HttpPost("products")]
    public async Task<ActionResult<ApiResponse<ProductTemplate>>> CreateProduct([FromBody] CreateProductRequest request)
    {
        if (request == null || request.GameId <= 0 || string.IsNullOrEmpty(request.Title) || request.BasePrice <= 0)
        {
            return BadRequest(ApiResponse<ProductTemplate>.ErrorResponse("Invalid product data"));
        }

        var product = new ProductTemplate
        {
            GameId = request.GameId,
            Title = request.Title,
            Description = request.Description,
            BasePrice = request.BasePrice,
            IsFeatured = request.IsFeatured,
            TagRank = request.TagRank,
            TagSkins = request.TagSkins,
            TagCollection = request.TagCollection,
            ImageUrl = request.ImageUrl
        };

        _context.ProductTemplates.Add(product);
        await _context.SaveChangesAsync();

        // Create inventory package
        var inventory = new InventoryPackage
        {
            TemplateId = product.TemplateId,
            QuantityAvailable = 0,
            Price = request.BasePrice,
            LastUpdated = DateTime.Now
        };

        _context.InventoryPackages.Add(inventory);
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<ProductTemplate>.SuccessResponse(product, "Product created successfully"));
    }

    // PUT: api/admin/products/{id} - Cập nhật sản phẩm
    [HttpPut("products/{id}")]
    public async Task<ActionResult<ApiResponse<ProductTemplate>>> UpdateProduct(int id, [FromBody] UpdateProductRequest request)
    {
        var product = await _context.ProductTemplates.FindAsync(id);
        if (product == null)
        {
            return NotFound(ApiResponse<ProductTemplate>.ErrorResponse("Product not found"));
        }

        if (request.GameId.HasValue) product.GameId = request.GameId.Value;
        if (!string.IsNullOrEmpty(request.Title)) product.Title = request.Title;
        if (request.Description != null) product.Description = request.Description;
        if (request.BasePrice.HasValue) product.BasePrice = request.BasePrice.Value;
        if (request.IsFeatured.HasValue) product.IsFeatured = request.IsFeatured.Value;
        if (request.TagRank != null) product.TagRank = request.TagRank;
        if (request.TagSkins != null) product.TagSkins = request.TagSkins;
        if (request.TagCollection != null) product.TagCollection = request.TagCollection;
        if (request.ImageUrl != null) product.ImageUrl = request.ImageUrl;

        await _context.SaveChangesAsync();

        return Ok(ApiResponse<ProductTemplate>.SuccessResponse(product, "Product updated successfully"));
    }

    // DELETE: api/admin/products/{id} - Xóa sản phẩm
    [HttpDelete("products/{id}")]
    public async Task<ActionResult<ApiResponse<object>>> DeleteProduct(int id)
    {
        var product = await _context.ProductTemplates.FindAsync(id);
        if (product == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("Product not found"));
        }

        // Check if product has orders
        var hasOrders = await _context.Orders
            .Include(o => o.AccountDetail)
            .AnyAsync(o => o.AccountDetail != null && o.AccountDetail.TemplateId == id);

        if (hasOrders)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Cannot delete product that has orders"));
        }

        _context.ProductTemplates.Remove(product);
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<object>.SuccessResponse(null, "Product deleted successfully"));
    }

    // ============================================
    // ADMIN - REVIEWS
    // ============================================

    // GET: api/admin/reviews/pending - Reviews chờ duyệt
    [HttpGet("reviews/pending")]
    public async Task<ActionResult<ApiResponse<List<object>>>> GetPendingReviews()
    {
        var reviews = await _context.Reviews
            .Include(r => r.User)
            .Include(r => r.ProductTemplate)
            .Where(r => !r.IsApproved)
            .OrderBy(r => r.CreatedAt)
            .ToListAsync();

        var result = reviews.Select(r => new
        {
            r.ReviewId,
            r.UserId,
            Username = r.User?.Username,
            r.TemplateId,
            ProductTitle = r.ProductTemplate?.Title,
            r.Rating,
            r.Comment,
            r.CreatedAt,
            r.IsApproved
        }).ToList();

        return Ok(ApiResponse<List<object>>.SuccessResponse(result.Cast<object>().ToList()));
    }

    // PUT: api/admin/reviews/{id}/approve - Duyệt review
    [HttpPut("reviews/{id}/approve")]
    public async Task<ActionResult<ApiResponse<Review>>> ApproveReview(int id)
    {
        var review = await _context.Reviews.FindAsync(id);
        if (review == null)
        {
            return NotFound(ApiResponse<Review>.ErrorResponse("Review not found"));
        }

        review.IsApproved = true;
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<Review>.SuccessResponse(review, "Review approved"));
    }

    // PUT: api/admin/reviews/{id}/reject - Từ chối review
    [HttpPut("reviews/{id}/reject")]
    public async Task<ActionResult<ApiResponse<object>>> RejectReview(int id)
    {
        var review = await _context.Reviews.FindAsync(id);
        if (review == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("Review not found"));
        }

        _context.Reviews.Remove(review);
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<object>.SuccessResponse(null, "Review rejected and deleted"));
    }

    // DELETE: api/admin/reviews/{id} - Xóa review
    [HttpDelete("reviews/{id}")]
    public async Task<ActionResult<ApiResponse<object>>> DeleteReview(int id)
    {
        var review = await _context.Reviews.FindAsync(id);
        if (review == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("Review not found"));
        }

        _context.Reviews.Remove(review);
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<object>.SuccessResponse(null, "Review deleted"));
    }

    // ============================================
    // ADMIN - USERS
    // ============================================

    // GET: api/admin/users - Danh sách tất cả users
    [HttpGet("users")]
    public async Task<ActionResult<ApiResponse<List<object>>>> GetAllUsers(
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 20)
    {
        var query = _context.Users.AsQueryable();

        var totalCount = await query.CountAsync();
        var users = await query
            .OrderByDescending(u => u.CreatedAt)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync();

        var result = users.Select(u => new
        {
            u.UserId,
            u.Username,
            u.Email,
            u.FullName,
            u.PhoneNumber,
            u.IsAdmin,
            u.Balance,
            u.CreatedAt,
            u.LastLogin
        }).ToList();

        return Ok(ApiResponse<List<object>>.SuccessResponse(result.Cast<object>().ToList()));
    }

    // PUT: api/admin/users/{id}/balance - Nạp tiền cho user
    [HttpPut("users/{id}/balance")]
    public async Task<ActionResult<ApiResponse<User>>> AddBalance(int id, [FromBody] AddBalanceRequest request)
    {
        if (request == null || request.Amount <= 0)
        {
            return BadRequest(ApiResponse<User>.ErrorResponse("Invalid amount"));
        }

        var user = await _context.Users.FindAsync(id);
        if (user == null)
        {
            return NotFound(ApiResponse<User>.ErrorResponse("User not found"));
        }

        user.Balance += request.Amount;
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<User>.SuccessResponse(user, $"Balance added: {request.Amount}"));
    }

    // PUT: api/admin/users/{id}/toggle-admin - Thêm/xóa quyền admin
    [HttpPut("users/{id}/toggle-admin")]
    public async Task<ActionResult<ApiResponse<User>>> ToggleAdmin(int id)
    {
        var user = await _context.Users.FindAsync(id);
        if (user == null)
        {
            return NotFound(ApiResponse<User>.ErrorResponse("User not found"));
        }

        user.IsAdmin = !user.IsAdmin;
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<User>.SuccessResponse(user, $"Admin status: {user.IsAdmin}"));
    }

    // ============================================
    // ADMIN - STATISTICS
    // ============================================

    // GET: api/admin/statistics - Thống kê tổng quan
    [HttpGet("statistics")]
    public async Task<ActionResult<ApiResponse<object>>> GetStatistics()
    {
        var totalUsers = await _context.Users.CountAsync();
        var totalOrders = await _context.Orders.CountAsync();
        var totalProducts = await _context.ProductTemplates.CountAsync();
        var totalRevenue = await _context.Orders
            .Where(o => o.Status == "completed")
            .SumAsync(o => (decimal?)o.TotalAmount) ?? 0;

        var ordersByStatus = await _context.Orders
            .GroupBy(o => o.Status)
            .Select(g => new { Status = g.Key, Count = g.Count() })
            .ToListAsync();

        var recentOrders = await _context.Orders
            .Where(o => o.OrderDate >= DateTime.Now.AddDays(-7))
            .CountAsync();

        var statistics = new
        {
            TotalUsers = totalUsers,
            TotalOrders = totalOrders,
            TotalProducts = totalProducts,
            TotalRevenue = totalRevenue,
            OrdersByStatus = ordersByStatus,
            RecentOrdersLast7Days = recentOrders
        };

        return Ok(ApiResponse<object>.SuccessResponse(statistics));
    }
}

// DTOs for Admin
public class CreateProductRequest
{
    public int GameId { get; set; }
    public string Title { get; set; } = string.Empty;
    public string? Description { get; set; }
    public decimal BasePrice { get; set; }
    public bool IsFeatured { get; set; } = false;
    public string? TagRank { get; set; }
    public string? TagSkins { get; set; }
    public string? TagCollection { get; set; }
    public string? ImageUrl { get; set; }
}

public class UpdateProductRequest
{
    public int? GameId { get; set; }
    public string? Title { get; set; }
    public string? Description { get; set; }
    public decimal? BasePrice { get; set; }
    public bool? IsFeatured { get; set; }
    public string? TagRank { get; set; }
    public string? TagSkins { get; set; }
    public string? TagCollection { get; set; }
    public string? ImageUrl { get; set; }
}

public class AddBalanceRequest
{
    public decimal Amount { get; set; }
}
