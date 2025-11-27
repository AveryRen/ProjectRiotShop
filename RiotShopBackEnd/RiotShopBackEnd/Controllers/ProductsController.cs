using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using RiotShopBackEnd.Data;
using RiotShopBackEnd.DTOs;
using RiotShopBackEnd.Models;

namespace RiotShopBackEnd.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ProductsController : ControllerBase
{
    private readonly ApplicationDbContext _context;

    public ProductsController(ApplicationDbContext context)
    {
        _context = context;
    }

    // GET: api/products - Lấy tất cả products với inventory
    [HttpGet]
    public async Task<ActionResult<ApiResponse<List<object>>>> GetProducts(
        [FromQuery] int? gameId,
        [FromQuery] bool? isFeatured,
        [FromQuery] string? search)
    {
        var query = _context.ProductTemplates
            .Include(pt => pt.GameType)
            .AsQueryable();

        if (gameId.HasValue)
        {
            query = query.Where(pt => pt.GameId == gameId.Value);
        }

        if (isFeatured.HasValue)
        {
            query = query.Where(pt => pt.IsFeatured == isFeatured.Value);
        }

        if (!string.IsNullOrEmpty(search))
        {
            query = query.Where(pt => pt.Title.Contains(search) || 
                                     (pt.Description != null && pt.Description.Contains(search)));
        }

        var products = await query.ToListAsync();
        
        // Get all inventories in one query
        var templateIds = products.Select(p => p.TemplateId).ToList();
        var inventories = await _context.InventoryPackages
            .Where(ip => templateIds.Contains(ip.TemplateId))
            .ToListAsync();
        
        var result = products.Select(p =>
        {
            var inventory = inventories.FirstOrDefault(ip => ip.TemplateId == p.TemplateId);
            return new
            {
                p.TemplateId,
                p.GameId,
                GameName = p.GameType?.Name,
                p.Title,
                p.Description,
                p.BasePrice,
                p.IsFeatured,
                p.TagRank,
                p.TagSkins,
                p.TagCollection,
                Inventory = inventory != null ? new
                {
                    inventory.PackageId,
                    inventory.QuantityAvailable,
                    inventory.Price,
                    inventory.LastUpdated
                } : null
            };
        }).ToList();

        return Ok(ApiResponse<List<object>>.SuccessResponse(result.Cast<object>().ToList()));
    }

    // GET: api/products/{id} - Lấy chi tiết product
    [HttpGet("{id}")]
    public async Task<ActionResult<ApiResponse<object>>> GetProduct(int id)
    {
        var product = await _context.ProductTemplates
            .Include(pt => pt.GameType)
            .FirstOrDefaultAsync(pt => pt.TemplateId == id);

        if (product == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("Product not found"));
        }

        var inventory = await _context.InventoryPackages
            .FirstOrDefaultAsync(ip => ip.TemplateId == id);

        var result = new
        {
            product.TemplateId,
            product.GameId,
            GameName = product.GameType?.Name,
            product.Title,
            product.Description,
            product.BasePrice,
            product.IsFeatured,
            product.TagRank,
            product.TagSkins,
            product.TagCollection,
            Inventory = inventory != null ? new
            {
                inventory.PackageId,
                inventory.QuantityAvailable,
                inventory.Price,
                inventory.LastUpdated
            } : null
        };

        return Ok(ApiResponse<object>.SuccessResponse(result));
    }

    // GET: api/products/games - Lấy danh sách games
    [HttpGet("games")]
    public async Task<ActionResult<ApiResponse<List<GameType>>>> GetGames()
    {
        var games = await _context.GameTypes.ToListAsync();
        return Ok(ApiResponse<List<GameType>>.SuccessResponse(games));
    }

    // GET: api/products/{id}/related - Sản phẩm liên quan (cùng game)
    [HttpGet("{id}/related")]
    public async Task<ActionResult<ApiResponse<List<object>>>> GetRelatedProducts(int id, [FromQuery] int limit = 5)
    {
        var product = await _context.ProductTemplates.FindAsync(id);
        if (product == null)
        {
            return NotFound(ApiResponse<List<object>>.ErrorResponse("Product not found"));
        }

        var relatedProducts = await _context.ProductTemplates
            .Include(pt => pt.GameType)
            .Where(pt => pt.GameId == product.GameId && pt.TemplateId != id)
            .Take(limit)
            .ToListAsync();

        var templateIds = relatedProducts.Select(p => p.TemplateId).ToList();
        var inventories = await _context.InventoryPackages
            .Where(ip => templateIds.Contains(ip.TemplateId))
            .ToListAsync();

        var result = relatedProducts.Select(p =>
        {
            var inventory = inventories.FirstOrDefault(ip => ip.TemplateId == p.TemplateId);
            return new
            {
                p.TemplateId,
                p.GameId,
                GameName = p.GameType?.Name,
                p.Title,
                p.BasePrice,
                p.IsFeatured,
                Inventory = inventory != null ? new
                {
                    inventory.QuantityAvailable,
                    inventory.Price
                } : null
            };
        }).ToList();

        return Ok(ApiResponse<List<object>>.SuccessResponse(result.Cast<object>().ToList()));
    }

    // GET: api/products/{id}/available-account - Lấy một tài khoản available cho template
    [HttpGet("{id}/available-account")]
    [AllowAnonymous]
    public async Task<ActionResult<ApiResponse<object>>> GetAvailableAccount(int id)
    {
        var availableAccount = await _context.AccountDetails
            .Include(ad => ad.ProductTemplate)
            .Where(ad => ad.TemplateId == id && ad.IsSold == false)
            .OrderBy(ad => ad.AccDetailId) // Lấy account đầu tiên
            .FirstOrDefaultAsync();

        if (availableAccount == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("No available account for this product"));
        }

        var result = new
        {
            availableAccount.AccDetailId,
            availableAccount.AccountUsername,
            availableAccount.RiotId,
            ProductTemplate = availableAccount.ProductTemplate != null ? new
            {
                availableAccount.ProductTemplate.TemplateId,
                availableAccount.ProductTemplate.Title,
                availableAccount.ProductTemplate.BasePrice
            } : null
        };

        return Ok(ApiResponse<object>.SuccessResponse(result));
    }
}
