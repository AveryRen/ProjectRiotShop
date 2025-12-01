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
public class ReviewsController : ControllerBase
{
    private readonly ApplicationDbContext _context;

    public ReviewsController(ApplicationDbContext context)
    {
        _context = context;
    }

    // GET: api/reviews/template/{templateId} - Public: Xem reviews (hiển thị ngay, không cần approve)
    [HttpGet("template/{templateId}")]
    [AllowAnonymous]
    public async Task<ActionResult<ApiResponse<List<object>>>> GetReviewsByTemplate(int templateId)
    {
        var reviews = await _context.Reviews
            .Include(r => r.User)
            .Where(r => r.TemplateId == templateId)
            .OrderByDescending(r => r.CreatedAt)
            .ToListAsync();

        var result = reviews.Select(r => new
        {
            r.ReviewId,
            r.UserId,
            Username = r.User?.Username,
            r.TemplateId,
            r.Rating,
            r.Comment,
            r.CreatedAt,
            r.IsApproved
        }).ToList();

        return Ok(ApiResponse<List<object>>.SuccessResponse(result.Cast<object>().ToList()));
    }

    // GET: api/reviews/me - User: Xem reviews của mình (kể cả chưa approve)
    [HttpGet("me")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<List<object>>>> GetMyReviews()
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<List<object>>.ErrorResponse("Unauthorized"));
        }

        var reviews = await _context.Reviews
            .Include(r => r.ProductTemplate)
            .Where(r => r.UserId == userId.Value)
            .OrderByDescending(r => r.CreatedAt)
            .ToListAsync();

        var result = reviews.Select(r => new
        {
            r.ReviewId,
            r.UserId,
            r.TemplateId,
            ProductTitle = r.ProductTemplate?.Title,
            r.Rating,
            r.Comment,
            r.CreatedAt,
            r.IsApproved
        }).ToList();

        return Ok(ApiResponse<List<object>>.SuccessResponse(result.Cast<object>().ToList()));
    }

    // GET: api/reviews/me/template/{templateId} - User: Xem review của mình cho sản phẩm cụ thể
    [HttpGet("me/template/{templateId}")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<object>>> GetMyReviewForTemplate(int templateId)
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<object>.ErrorResponse("Unauthorized"));
        }

        var review = await _context.Reviews
            .FirstOrDefaultAsync(r => r.UserId == userId.Value && r.TemplateId == templateId);

        if (review == null)
        {
            return Ok(ApiResponse<object>.SuccessResponse(null, "No review found"));
        }

        var result = new
        {
            review.ReviewId,
            review.UserId,
            review.TemplateId,
            review.Rating,
            review.Comment,
            review.CreatedAt,
            review.IsApproved
        };

        return Ok(ApiResponse<object>.SuccessResponse(result));
    }

    // PUT: api/reviews/{id} - User: Cập nhật review của mình
    [HttpPut("{id}")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<Review>>> UpdateReview(int id, [FromBody] UpdateReviewRequest request)
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<Review>.ErrorResponse("Unauthorized"));
        }

        var review = await _context.Reviews
            .FirstOrDefaultAsync(r => r.ReviewId == id && r.UserId == userId.Value);

        if (review == null)
        {
            return NotFound(ApiResponse<Review>.ErrorResponse("Review not found or you don't have permission to update it"));
        }

        if (request == null || 
            request.Rating < 1 || request.Rating > 5 || string.IsNullOrEmpty(request.Comment))
        {
            return BadRequest(ApiResponse<Review>.ErrorResponse("Invalid review data"));
        }

        review.Rating = request.Rating;
        review.Comment = request.Comment;
        review.CreatedAt = DateTime.Now; // Update timestamp

        await _context.SaveChangesAsync();

        return Ok(ApiResponse<Review>.SuccessResponse(review, "Review updated successfully"));
    }

    // DELETE: api/reviews/{id} - User: Xóa review của mình
    [HttpDelete("{id}")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<object>>> DeleteReview(int id)
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<object>.ErrorResponse("Unauthorized"));
        }

        var review = await _context.Reviews
            .FirstOrDefaultAsync(r => r.ReviewId == id && r.UserId == userId.Value);

        if (review == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("Review not found or you don't have permission to delete it"));
        }

        _context.Reviews.Remove(review);
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<object>.SuccessResponse(null, "Review deleted successfully"));
    }

    // POST: api/reviews - User: Tạo review (lấy userId từ token)
    [HttpPost]
    [Authorize]
    public async Task<ActionResult<ApiResponse<Review>>> CreateReview([FromBody] CreateReviewRequest request)
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<Review>.ErrorResponse("Unauthorized"));
        }

        if (request == null || request.TemplateId <= 0 || 
            request.Rating < 1 || request.Rating > 5 || string.IsNullOrEmpty(request.Comment))
        {
            return BadRequest(ApiResponse<Review>.ErrorResponse("Invalid review data"));
        }

        // Check if user already reviewed this product
        var existingReview = await _context.Reviews
            .FirstOrDefaultAsync(r => r.UserId == userId.Value && r.TemplateId == request.TemplateId);

        if (existingReview != null)
        {
            // Update existing review
            existingReview.Rating = request.Rating;
            existingReview.Comment = request.Comment;
            existingReview.CreatedAt = DateTime.Now; // Update timestamp
            existingReview.IsApproved = true; // Auto approve
            
            await _context.SaveChangesAsync();
            
            return Ok(ApiResponse<Review>.SuccessResponse(existingReview, "Review updated successfully"));
        }

        // Create new review if doesn't exist
        var review = new Review
        {
            UserId = userId.Value, // Lấy từ token, không dùng từ request
            TemplateId = request.TemplateId,
            Rating = request.Rating,
            Comment = request.Comment,
            CreatedAt = DateTime.Now,
            IsApproved = true // Auto approve - hiển thị ngay
        };

        _context.Reviews.Add(review);
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<Review>.SuccessResponse(review, "Review created successfully"));
    }
}

public class CreateReviewRequest
{
    public int TemplateId { get; set; }
    public int Rating { get; set; }
    public string Comment { get; set; } = string.Empty;
}

public class UpdateReviewRequest
{
    public int Rating { get; set; }
    public string Comment { get; set; } = string.Empty;
}
