using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Authorization;
using RiotShopBackEnd.Attributes;
using RiotShopBackEnd.DTOs;
using CloudinaryDotNet;
using CloudinaryDotNet.Actions;

namespace RiotShopBackEnd.Controllers;

[ApiController]
[Route("api/[controller]")]
public class FileUploadController : ControllerBase
{
    private readonly Cloudinary _cloudinary;

    public FileUploadController(Cloudinary cloudinary)
    {
        _cloudinary = cloudinary;
    }

    // POST: api/fileupload/image - Upload ảnh (Admin only - cho sản phẩm)
    [HttpPost("image")]
    [AdminOnly]
    public async Task<ActionResult<ApiResponse<object>>> UploadImage(IFormFile file)
    {
        if (file == null || file.Length == 0)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("No file uploaded"));
        }

        // Validate file type
        var allowedExtensions = new[] { ".jpg", ".jpeg", ".png", ".gif", ".webp" };
        var fileExtension = Path.GetExtension(file.FileName).ToLowerInvariant();
        
        if (!allowedExtensions.Contains(fileExtension))
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Invalid file type. Only images are allowed."));
        }

        // Validate file size (max 5MB)
        if (file.Length > 5 * 1024 * 1024)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("File size exceeds 5MB limit"));
        }

        try
        {
            // Upload to Cloudinary
            using (var stream = file.OpenReadStream())
            {
                var uploadParams = new ImageUploadParams()
                {
                    File = new FileDescription(file.FileName, stream),
                    Folder = "riotshop/products", // Organize in Cloudinary
                    PublicId = Guid.NewGuid().ToString(),
                    Overwrite = false
                };

                var uploadResult = await _cloudinary.UploadAsync(uploadParams);

                if (uploadResult.StatusCode == System.Net.HttpStatusCode.OK)
                {
                    var imageUrl = uploadResult.SecureUrl.ToString(); // Use HTTPS URL
                    return Ok(ApiResponse<object>.SuccessResponse(
                        new { url = imageUrl, publicId = uploadResult.PublicId }, 
                        "Image uploaded successfully to Cloudinary"
                    ));
                }
                else
                {
                    return StatusCode(500, ApiResponse<object>.ErrorResponse($"Cloudinary upload failed: {uploadResult.Error?.Message}"));
                }
            }
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse<object>.ErrorResponse($"Error uploading file: {ex.Message}"));
        }
    }

    // POST: api/fileupload/avatar - Upload avatar (User có thể upload cho chính mình)
    [HttpPost("avatar")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<object>>> UploadAvatar(IFormFile file)
    {
        if (file == null || file.Length == 0)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("No file uploaded"));
        }

        // Validate file type
        var allowedExtensions = new[] { ".jpg", ".jpeg", ".png", ".gif", ".webp" };
        var fileExtension = Path.GetExtension(file.FileName).ToLowerInvariant();
        
        if (!allowedExtensions.Contains(fileExtension))
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Invalid file type. Only images are allowed."));
        }

        // Validate file size (max 5MB)
        if (file.Length > 5 * 1024 * 1024)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("File size exceeds 5MB limit"));
        }

        try
        {
            // Upload to Cloudinary
            using (var stream = file.OpenReadStream())
            {
                var uploadParams = new ImageUploadParams()
                {
                    File = new FileDescription(file.FileName, stream),
                    Folder = "riotshop/avatars", // Organize in Cloudinary
                    PublicId = Guid.NewGuid().ToString(),
                    Overwrite = false,
                    Transformation = new Transformation().Width(400).Height(400).Crop("fill").Gravity("face") // Optimize for avatars
                };

                var uploadResult = await _cloudinary.UploadAsync(uploadParams);

                if (uploadResult.StatusCode == System.Net.HttpStatusCode.OK)
                {
                    var avatarUrl = uploadResult.SecureUrl.ToString(); // Use HTTPS URL
                    return Ok(ApiResponse<object>.SuccessResponse(
                        new { url = avatarUrl, publicId = uploadResult.PublicId }, 
                        "Avatar uploaded successfully to Cloudinary"
                    ));
                }
                else
                {
                    return StatusCode(500, ApiResponse<object>.ErrorResponse($"Cloudinary upload failed: {uploadResult.Error?.Message}"));
                }
            }
        }
        catch (Exception ex)
        {
            return StatusCode(500, ApiResponse<object>.ErrorResponse($"Error uploading file: {ex.Message}"));
        }
    }
}

