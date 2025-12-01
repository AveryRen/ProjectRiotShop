using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using RiotShopBackEnd.Data;
using RiotShopBackEnd.DTOs;
using RiotShopBackEnd.Helpers;
using RiotShopBackEnd.Models;
using Stripe;

namespace RiotShopBackEnd.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class PaymentController : ControllerBase
{
    private readonly ApplicationDbContext _context;
    private readonly IConfiguration _configuration;

    public PaymentController(ApplicationDbContext context, IConfiguration configuration)
    {
        _context = context;
        _configuration = configuration;
        
        // Initialize Stripe API key
        var stripeSecretKey = _configuration["Stripe:SecretKey"];
        if (!string.IsNullOrEmpty(stripeSecretKey))
        {
            StripeConfiguration.ApiKey = stripeSecretKey;
        }
    }

    // POST: api/payment/create-intent - Tạo payment intent
    [HttpPost("create-intent")]
    public async Task<ActionResult<ApiResponse<object>>> CreatePaymentIntent([FromBody] CreatePaymentIntentRequest request)
    {
        if (request == null || request.Amount <= 0)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Invalid amount. Amount must be greater than 0"));
        }

        // Minimum amount check for VND: 10,000 VND
        // Maximum amount check for VND: 25,000,000 VND
        if (request.Currency.ToUpper() == "VND")
        {
            if (request.Amount < 10000)
            {
                return BadRequest(ApiResponse<object>.ErrorResponse("Số tiền tối thiểu là 10.000₫"));
            }
            if (request.Amount > 25000000)
            {
                return BadRequest(ApiResponse<object>.ErrorResponse("Số tiền tối đa là 25.000.000₫"));
            }
        }
        else
        {
            // USD validation (fallback)
            if (request.Amount < 1)
            {
                return BadRequest(ApiResponse<object>.ErrorResponse("Minimum deposit amount is $1"));
            }
            if (request.Amount > 1000)
            {
                return BadRequest(ApiResponse<object>.ErrorResponse("Maximum deposit amount is $1000"));
            }
        }

        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<object>.ErrorResponse("Unauthorized"));
        }

        var user = await _context.Users.FindAsync(userId.Value);
        if (user == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("User not found"));
        }

        try
        {
            // Create Payment Intent
            // Note: VND doesn't have subunits (cents), so don't multiply by 100
            // USD and other currencies have cents, so multiply by 100
            long amountInSmallestUnit = request.Currency.ToUpper() == "VND" 
                ? (long)request.Amount  // VND: no subunit
                : (long)(request.Amount * 100); // USD and others: convert to cents
            
            var options = new PaymentIntentCreateOptions
            {
                Amount = amountInSmallestUnit,
                Currency = request.Currency.ToLower(),
                PaymentMethodTypes = new List<string> { "card" },
                Metadata = new Dictionary<string, string>
                {
                    { "userId", userId.Value.ToString() },
                    { "username", user.Username }
                }
            };

            var service = new PaymentIntentService();
            var paymentIntent = service.Create(options);

            // Save transaction to database
            var transaction = new PaymentTransaction
            {
                UserId = userId.Value,
                StripePaymentIntentId = paymentIntent.Id,
                Amount = request.Amount,
                Currency = request.Currency,
                Status = "pending",
                CreatedAt = DateTime.Now
            };

            _context.PaymentTransactions.Add(transaction);
            await _context.SaveChangesAsync();

            // Return client secret for Android app
            var response = new
            {
                ClientSecret = paymentIntent.ClientSecret,
                PaymentIntentId = paymentIntent.Id,
                Amount = request.Amount,
                Currency = request.Currency
            };

            return Ok(ApiResponse<object>.SuccessResponse(response, "Payment intent created successfully"));
        }
        catch (StripeException ex)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse($"Stripe error: {ex.Message}"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse<object>.ErrorResponse($"Internal server error: {ex.Message}"));
        }
    }

    // POST: api/payment/confirm - Xác nhận thanh toán thành công
    [HttpPost("confirm")]
    public async Task<ActionResult<ApiResponse<object>>> ConfirmPayment([FromBody] ConfirmPaymentRequest request)
    {
        if (string.IsNullOrEmpty(request.PaymentIntentId))
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Payment intent ID is required"));
        }

        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<object>.ErrorResponse("Unauthorized"));
        }

        try
        {
            // Retrieve payment intent from Stripe
            var service = new PaymentIntentService();
            var paymentIntent = service.Get(request.PaymentIntentId);

            if (paymentIntent == null)
            {
                return NotFound(ApiResponse<object>.ErrorResponse("Payment intent not found"));
            }

            // Verify payment intent belongs to current user
            var transaction = await _context.PaymentTransactions
                .FirstOrDefaultAsync(t => t.StripePaymentIntentId == request.PaymentIntentId && t.UserId == userId.Value);

            if (transaction == null)
            {
                return NotFound(ApiResponse<object>.ErrorResponse("Transaction not found"));
            }

            // Check if payment was successful
            if (paymentIntent.Status == "succeeded")
            {
                // Update transaction status
                transaction.Status = "succeeded";
                transaction.CompletedAt = DateTime.Now;
                transaction.UpdatedAt = DateTime.Now;

                // Add balance to user account
                var user = await _context.Users.FindAsync(userId.Value);
                if (user != null)
                {
                    user.Balance += transaction.Amount;
                }

                await _context.SaveChangesAsync();

                var response = new
                {
                    TransactionId = transaction.TransactionId,
                    Amount = transaction.Amount,
                    NewBalance = user?.Balance ?? 0,
                    Status = "succeeded"
                };

                return Ok(ApiResponse<object>.SuccessResponse(response, "Payment confirmed and balance updated"));
            }
            else if (paymentIntent.Status == "canceled")
            {
                transaction.Status = "canceled";
                transaction.UpdatedAt = DateTime.Now;
                await _context.SaveChangesAsync();

                return BadRequest(ApiResponse<object>.ErrorResponse("Payment was canceled"));
            }
            else
            {
                transaction.Status = paymentIntent.Status;
                transaction.UpdatedAt = DateTime.Now;
                if (paymentIntent.LastPaymentError != null)
                {
                    transaction.FailureReason = paymentIntent.LastPaymentError.Message;
                }
                await _context.SaveChangesAsync();

                return BadRequest(ApiResponse<object>.ErrorResponse($"Payment status: {paymentIntent.Status}"));
            }
        }
        catch (StripeException ex)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse($"Stripe error: {ex.Message}"));
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse<object>.ErrorResponse($"Internal server error: {ex.Message}"));
        }
    }

    // GET: api/payment/transactions - Lấy lịch sử giao dịch
    [HttpGet("transactions")]
    public async Task<ActionResult<ApiResponse<List<object>>>> GetTransactions(
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 20)
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<object>.ErrorResponse("Unauthorized"));
        }

        var transactions = await _context.PaymentTransactions
            .Where(t => t.UserId == userId.Value)
            .OrderByDescending(t => t.CreatedAt)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync();

        var result = transactions.Select(t => new
        {
            t.TransactionId,
            t.Amount,
            t.Currency,
            t.Status,
            t.CreatedAt,
            t.CompletedAt,
            t.FailureReason
        }).ToList();

        return Ok(ApiResponse<List<object>>.SuccessResponse(result.Cast<object>().ToList()));
    }

    // POST: api/payment/webhook - Webhook endpoint for Stripe (optional, for production)
    [HttpPost("webhook")]
    [AllowAnonymous]
    public async Task<IActionResult> StripeWebhook()
    {
        var json = await new StreamReader(HttpContext.Request.Body).ReadToEndAsync();
        var stripeSignature = Request.Headers["Stripe-Signature"].FirstOrDefault();
        var webhookSecret = _configuration["Stripe:WebhookSecret"];

        if (string.IsNullOrEmpty(webhookSecret))
        {
            return BadRequest("Webhook secret not configured");
        }

        try
        {
            var stripeEvent = EventUtility.ConstructEvent(json, stripeSignature, webhookSecret);

            if (stripeEvent.Type == Events.PaymentIntentSucceeded)
            {
                var paymentIntent = stripeEvent.Data.Object as PaymentIntent;
                if (paymentIntent != null)
                {
                    var transaction = await _context.PaymentTransactions
                        .FirstOrDefaultAsync(t => t.StripePaymentIntentId == paymentIntent.Id);

                    if (transaction != null && transaction.Status != "succeeded")
                    {
                        transaction.Status = "succeeded";
                        transaction.CompletedAt = DateTime.Now;
                        transaction.UpdatedAt = DateTime.Now;

                        var user = await _context.Users.FindAsync(transaction.UserId);
                        if (user != null)
                        {
                            user.Balance += transaction.Amount;
                        }

                        await _context.SaveChangesAsync();
                    }
                }
            }
            else if (stripeEvent.Type == Events.PaymentIntentPaymentFailed)
            {
                var paymentIntent = stripeEvent.Data.Object as PaymentIntent;
                if (paymentIntent != null)
                {
                    var transaction = await _context.PaymentTransactions
                        .FirstOrDefaultAsync(t => t.StripePaymentIntentId == paymentIntent.Id);

                    if (transaction != null)
                    {
                        transaction.Status = "failed";
                        transaction.UpdatedAt = DateTime.Now;
                        if (paymentIntent.LastPaymentError != null)
                        {
                            transaction.FailureReason = paymentIntent.LastPaymentError.Message;
                        }
                        await _context.SaveChangesAsync();
                    }
                }
            }

            return Ok();
        }
        catch (StripeException ex)
        {
            return BadRequest($"Stripe webhook error: {ex.Message}");
        }
        catch (Exception ex)
        {
            return StatusCode(500, $"Internal server error: {ex.Message}");
        }
    }
}

