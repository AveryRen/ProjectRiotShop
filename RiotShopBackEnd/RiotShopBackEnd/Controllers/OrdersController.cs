using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using RiotShopBackEnd.Attributes;
using RiotShopBackEnd.Data;
using RiotShopBackEnd.DTOs;
using RiotShopBackEnd.Helpers;
using RiotShopBackEnd.Models;

namespace RiotShopBackEnd.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class OrdersController : ControllerBase
{
    private readonly ApplicationDbContext _context;

    public OrdersController(ApplicationDbContext context)
    {
        _context = context;
    }

    // GET: api/orders/me - Lấy đơn hàng của user hiện tại
    [HttpGet("me")]
    public async Task<ActionResult<ApiResponse<List<object>>>> GetMyOrders()
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<List<object>>.ErrorResponse("Unauthorized"));
        }

        return await GetUserOrders(userId.Value);
    }

    // GET: api/orders/user/{userId} - Admin có thể xem đơn hàng của user khác
    [HttpGet("user/{userId}")]
    public async Task<ActionResult<ApiResponse<List<object>>>> GetUserOrders(int userId)
    {
        var currentUserId = UserHelper.GetUserId(User);
        var isAdmin = UserHelper.IsAdmin(User);

        // User chỉ xem được đơn hàng của mình, Admin xem được tất cả
        if (!isAdmin && currentUserId != userId)
        {
            return Forbid();
        }

        var orders = await _context.Orders
            .Include(o => o.AccountDetail)
                .ThenInclude(ad => ad!.ProductTemplate)
            .Where(o => o.UserId == userId)
            .OrderByDescending(o => o.OrderDate)
            .ToListAsync();

        var result = orders.Select(o => new
        {
            o.OrderId,
            o.UserId,
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

    // GET: api/orders/{id} - Chi tiết đơn hàng
    [HttpGet("{id}")]
    public async Task<ActionResult<ApiResponse<object>>> GetOrderDetails(int id)
    {
        var currentUserId = UserHelper.GetUserId(User);
        var isAdmin = UserHelper.IsAdmin(User);

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

        // User chỉ xem được đơn hàng của mình, Admin xem được tất cả
        if (!isAdmin && currentUserId != order.UserId)
        {
            return Forbid();
        }

        var result = new
        {
            order.OrderId,
            order.UserId,
            Username = order.User?.Username,
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

    // GET: api/orders/me/purchased - Lấy danh sách tài khoản đã mua (completed orders)
    [HttpGet("me/purchased")]
    public async Task<ActionResult<ApiResponse<List<object>>>> GetMyPurchasedAccounts()
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<List<object>>.ErrorResponse("Unauthorized"));
        }

        var orders = await _context.Orders
            .Include(o => o.AccountDetail)
                .ThenInclude(ad => ad!.ProductTemplate)
                    .ThenInclude(pt => pt!.GameType)
            .Where(o => o.UserId == userId.Value && o.Status == "completed")
            .OrderByDescending(o => o.OrderDate)
            .ToListAsync();

        var result = orders.Select(o => new
        {
            o.OrderId,
            o.OrderDate,
            o.TotalAmount,
            AccountDetail = o.AccountDetail != null ? new
            {
                o.AccountDetail.AccDetailId,
                o.AccountDetail.AccountUsername,
                o.AccountDetail.AccountPassword,
                o.AccountDetail.RiotId,
                o.AccountDetail.RecoveryEmail,
                ProductTemplate = o.AccountDetail.ProductTemplate != null ? new
                {
                    o.AccountDetail.ProductTemplate.TemplateId,
                    o.AccountDetail.ProductTemplate.Title,
                    o.AccountDetail.ProductTemplate.BasePrice,
                    GameName = o.AccountDetail.ProductTemplate.GameType?.Name,
                    GameType = o.AccountDetail.ProductTemplate.GameType != null ? new
                    {
                        o.AccountDetail.ProductTemplate.GameType.GameId,
                        o.AccountDetail.ProductTemplate.GameType.Name
                    } : null
                } : null
            } : null
        }).ToList();

        return Ok(ApiResponse<List<object>>.SuccessResponse(result.Cast<object>().ToList()));
    }

    // POST: api/orders - Tạo order mới (User chỉ tạo đơn hàng cho chính mình)
    [HttpPost]
    public async Task<ActionResult<ApiResponse<Order>>> CreateOrder([FromBody] CreateOrderRequest request)
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<Order>.ErrorResponse("Unauthorized"));
        }

        if (request == null || request.AccDetailId <= 0)
        {
            return BadRequest(ApiResponse<Order>.ErrorResponse("Invalid request data"));
        }

        // Check if account detail exists and is available
        var accountDetail = await _context.AccountDetails
            .Include(ad => ad.ProductTemplate)
            .FirstOrDefaultAsync(ad => ad.AccDetailId == request.AccDetailId);

        if (accountDetail == null)
        {
            return BadRequest(ApiResponse<Order>.ErrorResponse("Account detail not found"));
        }

        if (accountDetail.IsSold)
        {
            return BadRequest(ApiResponse<Order>.ErrorResponse("Account already sold"));
        }

        // Check user balance if payment method is Balance
        if (request.PaymentMethod == "Balance")
        {
            var user = await _context.Users.FindAsync(userId.Value);
            if (user == null)
            {
                return BadRequest(ApiResponse<Order>.ErrorResponse("User not found"));
            }

            if (user.Balance < request.TotalAmount)
            {
                return BadRequest(ApiResponse<Order>.ErrorResponse("Insufficient balance"));
            }

            // Deduct balance
            user.Balance -= request.TotalAmount;
        }

        // Generate transaction ID
        var transactionId = $"TXN{DateTime.Now:yyyyMMddHHmmss}{userId.Value}";

        var order = new Order
        {
            UserId = userId.Value, // Lấy từ token, không dùng từ request
            AccDetailId = request.AccDetailId,
            TotalAmount = request.TotalAmount,
            Status = "completed", // Đặt trạng thái completed ngay sau khi thanh toán thành công
            OrderDate = DateTime.Now,
            TransactionId = transactionId,
            PaymentMethod = request.PaymentMethod ?? "Balance",
            RefundStatus = "None"
        };

        // Mark account as sold - QUAN TRỌNG: Đánh dấu tài khoản đã bán để không bán lại
        accountDetail.IsSold = true;

        _context.Orders.Add(order);
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<Order>.SuccessResponse(order, "Order created successfully"));
    }

    // PUT: api/orders/{id}/status - Chỉ Admin mới được cập nhật
    [HttpPut("{id}/status")]
    [AdminOnly]
    public async Task<ActionResult<ApiResponse<Order>>> UpdateOrderStatus(int id, [FromBody] UpdateOrderStatusRequest request)
    {
        if (request == null || string.IsNullOrEmpty(request.Status))
        {
            return BadRequest(ApiResponse<Order>.ErrorResponse("Status is required"));
        }

        var order = await _context.Orders.FindAsync(id);
        if (order == null)
        {
            return NotFound(ApiResponse<Order>.ErrorResponse("Order not found"));
        }

        order.Status = request.Status;
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<Order>.SuccessResponse(order, "Order status updated"));
    }

    // POST: api/orders/{id}/cancel - User chỉ hủy được đơn hàng của mình
    [HttpPost("{id}/cancel")]
    public async Task<ActionResult<ApiResponse<Order>>> CancelOrder(int id, [FromBody] CancelOrderRequest? request)
    {
        var userId = UserHelper.GetUserId(User);
        var isAdmin = UserHelper.IsAdmin(User);

        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<Order>.ErrorResponse("Unauthorized"));
        }

        var order = await _context.Orders
            .Include(o => o.AccountDetail)
            .FirstOrDefaultAsync(o => o.OrderId == id);

        if (order == null)
        {
            return NotFound(ApiResponse<Order>.ErrorResponse("Order not found"));
        }

        // User chỉ hủy được đơn hàng của mình, Admin hủy được tất cả
        if (!isAdmin && order.UserId != userId.Value)
        {
            return Forbid();
        }

        if (order.Status == "cancelled")
        {
            return BadRequest(ApiResponse<Order>.ErrorResponse("Order already cancelled"));
        }

        // Refund if payment method is Balance
        if (order.PaymentMethod == "Balance" && order.Status == "completed")
        {
            var user = await _context.Users.FindAsync(order.UserId);
            if (user != null)
            {
                user.Balance += order.TotalAmount;
            }
        }

        // Mark account as available again
        if (order.AccountDetail != null)
        {
            order.AccountDetail.IsSold = false;
        }

        order.Status = "cancelled";
        order.RefundStatus = "Requested";
        
        // Lưu lý do hủy đơn hàng
        if (request != null && !string.IsNullOrEmpty(request.Reason))
        {
            order.CancelReason = request.Reason;
        }

        await _context.SaveChangesAsync();

        return Ok(ApiResponse<Order>.SuccessResponse(order, "Order cancelled successfully"));
    }
}

// DTOs for Orders
public class CreateOrderRequest
{
    public int AccDetailId { get; set; }
    public decimal TotalAmount { get; set; }
    public string? PaymentMethod { get; set; }
}

public class UpdateOrderStatusRequest
{
    public string Status { get; set; } = string.Empty;
}

public class CancelOrderRequest
{
    public string? Reason { get; set; }
}
