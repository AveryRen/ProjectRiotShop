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
public class AddressesController : ControllerBase
{
    private readonly ApplicationDbContext _context;

    public AddressesController(ApplicationDbContext context)
    {
        _context = context;
    }

    // GET: api/addresses/me - Danh sách địa chỉ của user hiện tại
    [HttpGet("me")]
    public async Task<ActionResult<ApiResponse<List<UserAddress>>>> GetMyAddresses()
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<List<UserAddress>>.ErrorResponse("Unauthorized"));
        }

        var addresses = await _context.UserAddresses
            .Where(ua => ua.UserId == userId.Value)
            .OrderByDescending(ua => ua.IsDefault)
            .ThenByDescending(ua => ua.CreatedAt)
            .ToListAsync();

        return Ok(ApiResponse<List<UserAddress>>.SuccessResponse(addresses));
    }

    // POST: api/addresses - Thêm địa chỉ mới
    [HttpPost]
    public async Task<ActionResult<ApiResponse<UserAddress>>> AddAddress([FromBody] CreateAddressRequest request)
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<UserAddress>.ErrorResponse("Unauthorized"));
        }

        if (request == null || string.IsNullOrEmpty(request.FullName) || 
            string.IsNullOrEmpty(request.PhoneNumber) || string.IsNullOrEmpty(request.AddressLine))
        {
            return BadRequest(ApiResponse<UserAddress>.ErrorResponse("Full name, phone number, and address line are required"));
        }

        // If this is set as default, unset other defaults
        if (request.IsDefault)
        {
            var existingDefaults = await _context.UserAddresses
                .Where(ua => ua.UserId == userId.Value && ua.IsDefault)
                .ToListAsync();

            foreach (var addr in existingDefaults)
            {
                addr.IsDefault = false;
            }
        }

        var address = new UserAddress
        {
            UserId = userId.Value,
            FullName = request.FullName,
            PhoneNumber = request.PhoneNumber,
            AddressLine = request.AddressLine,
            City = request.City,
            District = request.District,
            Ward = request.Ward,
            IsDefault = request.IsDefault,
            CreatedAt = DateTime.Now
        };

        _context.UserAddresses.Add(address);
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<UserAddress>.SuccessResponse(address, "Address added successfully"));
    }

    // PUT: api/addresses/{id} - Cập nhật địa chỉ
    [HttpPut("{id}")]
    public async Task<ActionResult<ApiResponse<UserAddress>>> UpdateAddress(int id, [FromBody] UpdateAddressRequest request)
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<UserAddress>.ErrorResponse("Unauthorized"));
        }

        var address = await _context.UserAddresses
            .FirstOrDefaultAsync(ua => ua.AddressId == id && ua.UserId == userId.Value);

        if (address == null)
        {
            return NotFound(ApiResponse<UserAddress>.ErrorResponse("Address not found"));
        }

        if (!string.IsNullOrEmpty(request.FullName)) address.FullName = request.FullName;
        if (!string.IsNullOrEmpty(request.PhoneNumber)) address.PhoneNumber = request.PhoneNumber;
        if (!string.IsNullOrEmpty(request.AddressLine)) address.AddressLine = request.AddressLine;
        if (request.City != null) address.City = request.City;
        if (request.District != null) address.District = request.District;
        if (request.Ward != null) address.Ward = request.Ward;

        // Handle default address
        if (request.IsDefault.HasValue && request.IsDefault.Value)
        {
            var existingDefaults = await _context.UserAddresses
                .Where(ua => ua.UserId == userId.Value && ua.IsDefault && ua.AddressId != id)
                .ToListAsync();

            foreach (var addr in existingDefaults)
            {
                addr.IsDefault = false;
            }

            address.IsDefault = true;
        }

        await _context.SaveChangesAsync();

        return Ok(ApiResponse<UserAddress>.SuccessResponse(address, "Address updated successfully"));
    }

    // DELETE: api/addresses/{id} - Xóa địa chỉ
    [HttpDelete("{id}")]
    public async Task<ActionResult<ApiResponse<object>>> DeleteAddress(int id)
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<object>.ErrorResponse("Unauthorized"));
        }

        var address = await _context.UserAddresses
            .FirstOrDefaultAsync(ua => ua.AddressId == id && ua.UserId == userId.Value);

        if (address == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("Address not found"));
        }

        _context.UserAddresses.Remove(address);
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<object>.SuccessResponse(null, "Address deleted successfully"));
    }

    // PUT: api/addresses/{id}/default - Đặt làm địa chỉ mặc định
    [HttpPut("{id}/default")]
    public async Task<ActionResult<ApiResponse<UserAddress>>> SetDefaultAddress(int id)
    {
        var userId = UserHelper.GetUserId(User);
        if (!userId.HasValue)
        {
            return Unauthorized(ApiResponse<UserAddress>.ErrorResponse("Unauthorized"));
        }

        var address = await _context.UserAddresses
            .FirstOrDefaultAsync(ua => ua.AddressId == id && ua.UserId == userId.Value);

        if (address == null)
        {
            return NotFound(ApiResponse<UserAddress>.ErrorResponse("Address not found"));
        }

        // Unset other defaults
        var existingDefaults = await _context.UserAddresses
            .Where(ua => ua.UserId == userId.Value && ua.IsDefault && ua.AddressId != id)
            .ToListAsync();

        foreach (var addr in existingDefaults)
        {
            addr.IsDefault = false;
        }

        address.IsDefault = true;
        await _context.SaveChangesAsync();

        return Ok(ApiResponse<UserAddress>.SuccessResponse(address, "Default address updated"));
    }
}

public class CreateAddressRequest
{
    public string FullName { get; set; } = string.Empty;
    public string PhoneNumber { get; set; } = string.Empty;
    public string AddressLine { get; set; } = string.Empty;
    public string? City { get; set; }
    public string? District { get; set; }
    public string? Ward { get; set; }
    public bool IsDefault { get; set; } = false;
}

public class UpdateAddressRequest
{
    public string? FullName { get; set; }
    public string? PhoneNumber { get; set; }
    public string? AddressLine { get; set; }
    public string? City { get; set; }
    public string? District { get; set; }
    public string? Ward { get; set; }
    public bool? IsDefault { get; set; }
}
