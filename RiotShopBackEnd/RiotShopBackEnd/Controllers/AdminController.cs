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

    // DELETE: api/admin/orders/{id} - Xóa đơn hàng
    [HttpDelete("orders/{id}")]
    public async Task<ActionResult<ApiResponse<object>>> DeleteOrder(int id)
    {
        var order = await _context.Orders
            .Include(o => o.AccountDetail)
            .FirstOrDefaultAsync(o => o.OrderId == id);

        if (order == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("Order not found"));
        }

        // If order is completed, mark account as available again
        if (order.Status == "completed" && order.AccountDetail != null)
        {
            order.AccountDetail.IsSold = false;
        }

        _context.Orders.Remove(order);
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<object>.SuccessResponse(null, "Order deleted successfully"));
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

        // Xóa tất cả inventory packages (AccountDetails) trước khi xóa product template
        var accountDetails = await _context.AccountDetails
            .Where(ad => ad.TemplateId == id)
            .ToListAsync();
        
        if (accountDetails.Any())
        {
            _context.AccountDetails.RemoveRange(accountDetails);
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

    // GET: api/admin/users - Danh sách tất cả users (trừ admin hiện tại)
    [HttpGet("users")]
    public async Task<ActionResult<ApiResponse<List<object>>>> GetAllUsers(
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 20)
    {
        // Lấy user ID của admin hiện tại từ JWT token
        var currentUserId = UserHelper.GetUserId(User);
        
        var query = _context.Users.AsQueryable();
        
        // Loại bỏ admin hiện tại khỏi danh sách
        if (currentUserId.HasValue)
        {
            query = query.Where(u => u.UserId != currentUserId.Value);
        }

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
        // Lấy user ID của admin hiện tại từ JWT token
        var currentUserId = UserHelper.GetUserId(User);
        
        // Ngăn admin thay đổi quyền admin của chính mình
        if (currentUserId.HasValue && id == currentUserId.Value)
        {
            return BadRequest(ApiResponse<User>.ErrorResponse("Bạn không thể thay đổi quyền admin của chính mình"));
        }

        var user = await _context.Users.FindAsync(id);
        if (user == null)
        {
            return NotFound(ApiResponse<User>.ErrorResponse("User not found"));
        }

        user.IsAdmin = !user.IsAdmin;
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<User>.SuccessResponse(user, $"Admin status: {user.IsAdmin}"));
    }

    // POST: api/admin/users - Tạo user mới (admin có thể tạo user hoặc admin)
    [HttpPost("users")]
    public async Task<ActionResult<ApiResponse<object>>> CreateUser([FromBody] CreateUserRequest request)
    {
        if (request == null || string.IsNullOrEmpty(request.Username) || 
            string.IsNullOrEmpty(request.Email) || string.IsNullOrEmpty(request.Password))
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Username, email and password are required"));
        }

        // Check if username already exists
        var usernameExists = await _context.Users.AnyAsync(u => u.Username == request.Username);
        if (usernameExists)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Username already exists"));
        }

        // Check if email already exists
        var emailExists = await _context.Users.AnyAsync(u => u.Email == request.Email);
        if (emailExists)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Email already exists"));
        }

        // Store password directly (matching AuthService behavior)
        var user = new User
        {
            Username = request.Username,
            Email = request.Email,
            PasswordHash = request.Password,
            FullName = request.FullName,
            PhoneNumber = request.PhoneNumber,
            Address = request.Address,
            IsAdmin = request.IsAdmin ?? false,
            Balance = request.Balance ?? 0,
            CreatedAt = DateTime.Now
        };

        _context.Users.Add(user);
        await _context.SaveChangesAsync();

        var result = new
        {
            user.UserId,
            user.Username,
            user.Email,
            user.FullName,
            user.PhoneNumber,
            user.IsAdmin,
            user.Balance,
            user.CreatedAt
        };

        return Ok(ApiResponse<object>.SuccessResponse(result, "User created successfully"));
    }

    // PUT: api/admin/users/{id} - Cập nhật user
    [HttpPut("users/{id}")]
    public async Task<ActionResult<ApiResponse<object>>> UpdateUser(int id, [FromBody] UpdateUserAdminRequest request)
    {
        // Lấy user ID của admin hiện tại từ JWT token
        var currentUserId = UserHelper.GetUserId(User);
        
        // Ngăn admin chỉnh sửa chính mình
        if (currentUserId.HasValue && id == currentUserId.Value)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Bạn không thể chỉnh sửa tài khoản của chính mình"));
        }

        var user = await _context.Users.FindAsync(id);
        if (user == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("User not found"));
        }

        if (!string.IsNullOrEmpty(request.Email))
        {
            var emailExists = await _context.Users
                .AnyAsync(u => u.Email == request.Email && u.UserId != id);
            if (emailExists)
            {
                return BadRequest(ApiResponse<object>.ErrorResponse("Email already exists"));
            }
            user.Email = request.Email;
        }

        if (!string.IsNullOrEmpty(request.Password))
        {
            // Store password directly (matching AuthService behavior)
            user.PasswordHash = request.Password;
        }

        if (request.FullName != null) user.FullName = request.FullName;
        if (request.PhoneNumber != null) user.PhoneNumber = request.PhoneNumber;
        if (request.Address != null) user.Address = request.Address;
        if (request.IsAdmin.HasValue) user.IsAdmin = request.IsAdmin.Value;
        if (request.Balance.HasValue) user.Balance = request.Balance.Value;

        await _context.SaveChangesAsync();

        var result = new
        {
            user.UserId,
            user.Username,
            user.Email,
            user.FullName,
            user.PhoneNumber,
            user.IsAdmin,
            user.Balance,
            user.CreatedAt
        };

        return Ok(ApiResponse<object>.SuccessResponse(result, "User updated successfully"));
    }

    // DELETE: api/admin/users/{id} - Xóa user
    [HttpDelete("users/{id}")]
    public async Task<ActionResult<ApiResponse<object>>> DeleteUser(int id)
    {
        // Lấy user ID của admin hiện tại từ JWT token
        var currentUserId = UserHelper.GetUserId(User);
        
        // Ngăn admin xóa chính mình
        if (currentUserId.HasValue && id == currentUserId.Value)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Bạn không thể xóa tài khoản của chính mình"));
        }

        var user = await _context.Users.FindAsync(id);
        if (user == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("User not found"));
        }

        // Check if user has orders
        var hasOrders = await _context.Orders.AnyAsync(o => o.UserId == id);
        if (hasOrders)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Cannot delete user that has orders"));
        }

        _context.Users.Remove(user);
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<object>.SuccessResponse(null, "User deleted successfully"));
    }

    // ============================================
    // ADMIN - ACCOUNT DETAILS (Tài khoản trong sản phẩm)
    // ============================================

    // GET: api/admin/products/{templateId}/accounts - Lấy danh sách tài khoản của sản phẩm
    [HttpGet("products/{templateId}/accounts")]
    public async Task<ActionResult<ApiResponse<List<object>>>> GetProductAccounts(int templateId)
    {
        var accounts = await _context.AccountDetails
            .Where(ad => ad.TemplateId == templateId)
            .OrderBy(ad => ad.IsSold)
            .ThenBy(ad => ad.AccDetailId)
            .ToListAsync();

        var result = accounts.Select(ad => new
        {
            ad.AccDetailId,
            ad.TemplateId,
            ad.AccountUsername,
            ad.RiotId,
            ad.IsSold,
            ad.RecoveryEmail,
            ad.IsDropMail,
            ad.OriginalPrice
        }).ToList();

        return Ok(ApiResponse<List<object>>.SuccessResponse(result.Cast<object>().ToList()));
    }

    // POST: api/admin/products/{templateId}/accounts - Tạo tài khoản mới cho sản phẩm
    [HttpPost("products/{templateId}/accounts")]
    public async Task<ActionResult<ApiResponse<object>>> CreateAccount(int templateId, [FromBody] CreateAccountRequest request)
    {
        // Check if product exists
        var product = await _context.ProductTemplates.FindAsync(templateId);
        if (product == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("Product not found"));
        }

        if (request == null || string.IsNullOrEmpty(request.AccountUsername) || 
            string.IsNullOrEmpty(request.AccountPassword))
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Account username and password are required"));
        }

        var account = new AccountDetail
        {
            TemplateId = templateId,
            AccountUsername = request.AccountUsername,
            AccountPassword = request.AccountPassword,
            RiotId = request.RiotId,
            RecoveryEmail = request.RecoveryEmail,
            IsDropMail = request.IsDropMail ?? false,
            OriginalPrice = request.OriginalPrice ?? product.BasePrice,
            IsSold = false
        };

        _context.AccountDetails.Add(account);
        await _context.SaveChangesAsync();

        // Update inventory quantity
        var inventory = await _context.InventoryPackages
            .FirstOrDefaultAsync(ip => ip.TemplateId == templateId);
        if (inventory != null)
        {
            inventory.QuantityAvailable = await _context.AccountDetails
                .CountAsync(ad => ad.TemplateId == templateId && !ad.IsSold);
            inventory.LastUpdated = DateTime.Now;
            await _context.SaveChangesAsync();
        }

        var result = new
        {
            account.AccDetailId,
            account.TemplateId,
            account.AccountUsername,
            account.RiotId,
            account.IsSold,
            account.RecoveryEmail,
            account.IsDropMail,
            account.OriginalPrice
        };

        return Ok(ApiResponse<object>.SuccessResponse(result, "Account created successfully"));
    }

    // PUT: api/admin/accounts/{id} - Cập nhật tài khoản
    [HttpPut("accounts/{id}")]
    public async Task<ActionResult<ApiResponse<object>>> UpdateAccount(int id, [FromBody] UpdateAccountRequest request)
    {
        var account = await _context.AccountDetails.FindAsync(id);
        if (account == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("Account not found"));
        }

        if (!string.IsNullOrEmpty(request.AccountUsername)) account.AccountUsername = request.AccountUsername;
        if (!string.IsNullOrEmpty(request.AccountPassword)) account.AccountPassword = request.AccountPassword;
        if (request.RiotId != null) account.RiotId = request.RiotId;
        if (request.RecoveryEmail != null) account.RecoveryEmail = request.RecoveryEmail;
        if (request.IsDropMail.HasValue) account.IsDropMail = request.IsDropMail.Value;
        if (request.OriginalPrice.HasValue) account.OriginalPrice = request.OriginalPrice.Value;
        if (request.IsSold.HasValue) account.IsSold = request.IsSold.Value;

        await _context.SaveChangesAsync();

        // Update inventory quantity
        var inventory = await _context.InventoryPackages
            .FirstOrDefaultAsync(ip => ip.TemplateId == account.TemplateId);
        if (inventory != null)
        {
            inventory.QuantityAvailable = await _context.AccountDetails
                .CountAsync(ad => ad.TemplateId == account.TemplateId && !ad.IsSold);
            inventory.LastUpdated = DateTime.Now;
            await _context.SaveChangesAsync();
        }

        var result = new
        {
            account.AccDetailId,
            account.TemplateId,
            account.AccountUsername,
            account.RiotId,
            account.IsSold,
            account.RecoveryEmail,
            account.IsDropMail,
            account.OriginalPrice
        };

        return Ok(ApiResponse<object>.SuccessResponse(result, "Account updated successfully"));
    }

    // POST: api/admin/products/{templateId}/accounts/bulk - Import nhiều tài khoản
    [HttpPost("products/{templateId}/accounts/bulk")]
    public async Task<ActionResult<ApiResponse<object>>> BulkCreateAccounts(int templateId, [FromBody] BulkCreateAccountsRequest request)
    {
        // Check if product exists
        var product = await _context.ProductTemplates.FindAsync(templateId);
        if (product == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("Product not found"));
        }

        if (request == null || request.Accounts == null || request.Accounts.Count == 0)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("No accounts provided"));
        }

        var createdAccounts = new List<object>();
        var errors = new List<string>();

        foreach (var acc in request.Accounts)
        {
            if (string.IsNullOrEmpty(acc.AccountUsername) || string.IsNullOrEmpty(acc.AccountPassword))
            {
                errors.Add($"Account missing username or password");
                continue;
            }

            try
            {
                var account = new AccountDetail
                {
                    TemplateId = templateId,
                    AccountUsername = acc.AccountUsername,
                    AccountPassword = acc.AccountPassword,
                    RiotId = acc.RiotId,
                    RecoveryEmail = acc.RecoveryEmail,
                    IsDropMail = acc.IsDropMail ?? false,
                    OriginalPrice = acc.OriginalPrice ?? product.BasePrice,
                    IsSold = false
                };

                _context.AccountDetails.Add(account);
                createdAccounts.Add(new { account.AccountUsername });
            }
            catch (Exception ex)
            {
                errors.Add($"Error creating account {acc.AccountUsername}: {ex.Message}");
            }
        }

        if (createdAccounts.Count > 0)
        {
            await _context.SaveChangesAsync();

            // Update inventory quantity
            var inventory = await _context.InventoryPackages
                .FirstOrDefaultAsync(ip => ip.TemplateId == templateId);
            if (inventory != null)
            {
                inventory.QuantityAvailable = await _context.AccountDetails
                    .CountAsync(ad => ad.TemplateId == templateId && !ad.IsSold);
                inventory.LastUpdated = DateTime.Now;
                await _context.SaveChangesAsync();
            }
        }

        var result = new
        {
            Created = createdAccounts.Count,
            Errors = errors,
            Accounts = createdAccounts
        };

        return Ok(ApiResponse<object>.SuccessResponse(result, $"Created {createdAccounts.Count} accounts"));
    }

    // DELETE: api/admin/accounts/{id} - Xóa tài khoản
    [HttpDelete("accounts/{id}")]
    public async Task<ActionResult<ApiResponse<object>>> DeleteAccount(int id)
    {
        var account = await _context.AccountDetails.FindAsync(id);
        if (account == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("Account not found"));
        }

        // Check if account is sold
        if (account.IsSold)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Cannot delete sold account"));
        }

        var templateId = account.TemplateId;
        _context.AccountDetails.Remove(account);
        await _context.SaveChangesAsync();

        // Update inventory quantity
        var inventory = await _context.InventoryPackages
            .FirstOrDefaultAsync(ip => ip.TemplateId == templateId);
        if (inventory != null)
        {
            inventory.QuantityAvailable = await _context.AccountDetails
                .CountAsync(ad => ad.TemplateId == templateId && !ad.IsSold);
            inventory.LastUpdated = DateTime.Now;
            await _context.SaveChangesAsync();
        }

        return Ok(ApiResponse<object>.SuccessResponse(null, "Account deleted successfully"));
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

public class CreateUserRequest
{
    public string Username { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
    public string? FullName { get; set; }
    public string? PhoneNumber { get; set; }
    public string? Address { get; set; }
    public bool? IsAdmin { get; set; }
    public decimal? Balance { get; set; }
}

public class UpdateUserAdminRequest
{
    public string? Email { get; set; }
    public string? Password { get; set; }
    public string? FullName { get; set; }
    public string? PhoneNumber { get; set; }
    public string? Address { get; set; }
    public bool? IsAdmin { get; set; }
    public decimal? Balance { get; set; }
}

public class CreateAccountRequest
{
    public string AccountUsername { get; set; } = string.Empty;
    public string AccountPassword { get; set; } = string.Empty;
    public string? RiotId { get; set; }
    public string? RecoveryEmail { get; set; }
    public bool? IsDropMail { get; set; }
    public decimal? OriginalPrice { get; set; }
}

public class UpdateAccountRequest
{
    public string? AccountUsername { get; set; }
    public string? AccountPassword { get; set; }
    public string? RiotId { get; set; }
    public string? RecoveryEmail { get; set; }
    public bool? IsDropMail { get; set; }
    public decimal? OriginalPrice { get; set; }
    public bool? IsSold { get; set; }
}

public class BulkCreateAccountsRequest
{
    public List<AccountInput> Accounts { get; set; } = new();
}

public class AccountInput
{
    public string AccountUsername { get; set; } = string.Empty;
    public string AccountPassword { get; set; } = string.Empty;
    public string? RiotId { get; set; }
    public string? RecoveryEmail { get; set; }
    public bool? IsDropMail { get; set; }
    public decimal? OriginalPrice { get; set; }
}
