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

    // GET: api/reviews/template/{templateId} - Public: Xem reviews (chỉ đã approve)
    [HttpGet("template/{templateId}")]
    [AllowAnonymous]
    public async Task<ActionResult<ApiResponse<List<object>>>> GetReviewsByTemplate(int templateId)
    {
        var reviews = await _context.Reviews
            .Include(r => r.User)
            .Where(r => r.TemplateId == templateId && r.IsApproved)
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
            return BadRequest(ApiResponse<Review>.ErrorResponse("You have already reviewed this product"));
        }

        var review = new Review
        {
            UserId = userId.Value, // Lấy từ token, không dùng từ request
            TemplateId = request.TemplateId,
            Rating = request.Rating,
            Comment = request.Comment,
            CreatedAt = DateTime.Now,
            IsApproved = false // Cần admin approve
        };

        _context.Reviews.Add(review);
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<Review>.SuccessResponse(review, "Review created successfully. Waiting for admin approval."));
    }
}

public class CreateReviewRequest
{
    public int TemplateId { get; set; }
    public int Rating { get; set; }
    public string Comment { get; set; } = string.Empty;
}
