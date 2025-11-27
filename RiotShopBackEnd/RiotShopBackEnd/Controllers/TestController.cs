using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using RiotShopBackEnd.Data;

namespace RiotShopBackEnd.Controllers;

[ApiController]
[Route("api/[controller]")]
public class TestController : ControllerBase
{
    private readonly ApplicationDbContext _context;
    private readonly IConfiguration _configuration;

    public TestController(ApplicationDbContext context, IConfiguration configuration)
    {
        _context = context;
        _configuration = configuration;
    }

    // GET: api/test/connection
    // Endpoint để test kết nối MySQL
    [HttpGet("connection")]
    public async Task<IActionResult> TestConnection()
    {
        try
        {
            var connectionString = _configuration.GetConnectionString("DefaultConnection");
            
            // Che giấu password trong response
            var safeConnectionString = connectionString;
            if (!string.IsNullOrEmpty(connectionString))
            {
                var parts = connectionString.Split(';');
                var safeParts = parts.Select(p => 
                    p.Trim().StartsWith("Password", StringComparison.OrdinalIgnoreCase) 
                        ? "Password=***" 
                        : p).ToArray();
                safeConnectionString = string.Join(";", safeParts);
            }

            // Test connection
            var canConnect = await _context.Database.CanConnectAsync();
            
            if (canConnect)
            {
                return Ok(new
                {
                    success = true,
                    message = "Kết nối MySQL thành công!",
                    connectionString = safeConnectionString,
                    database = _context.Database.GetDbConnection().Database
                });
            }
            else
            {
                return BadRequest(new
                {
                    success = false,
                    message = "Không thể kết nối đến database",
                    connectionString = safeConnectionString
                });
            }
        }
        catch (Exception ex)
        {
            return BadRequest(new
            {
                success = false,
                message = "Lỗi kết nối MySQL",
                error = ex.Message,
                hint = "Kiểm tra lại password trong appsettings.json"
            });
        }
    }
}
